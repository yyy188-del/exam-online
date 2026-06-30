package com.yy.exam.modules.exam.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.core.exception.ServiceException;
import com.yy.exam.core.utils.BeanMapper;
import com.yy.exam.modules.exam.dto.ExamDTO;
import com.yy.exam.modules.exam.dto.ExamRepoDTO;
import com.yy.exam.modules.exam.dto.ext.ExamRepoExtDTO;
import com.yy.exam.modules.exam.dto.request.ExamSaveReqDTO;
import com.yy.exam.modules.exam.dto.response.ExamOnlineRespDTO;
import com.yy.exam.modules.exam.dto.response.ExamReviewRespDTO;
import com.yy.exam.modules.exam.entity.Exam;
import com.yy.exam.modules.exam.mapper.ExamMapper;
import com.yy.exam.modules.exam.service.ExamRepoService;
import com.yy.exam.modules.exam.service.ExamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考试业务实现类
 * 【涉及功能：考试详情页标签页（创建/编辑考试）】
 * 
 * 职责：处理考试的业务逻辑，包括考试信息的保存、查询、分页等
 * 
 * 考试详情页标签页的核心逻辑：
 * - 保存考试时，需要同时处理两个标签页的数据：
 *   1. 基本信息（存入 exam 表）
 *   2. 题库设置（存入 exam_repo 表）
 * - 查询考试详情时，需要从两张表分别查询数据，组装成一个完整的 DTO
 * - 计算总分时，根据每个题库的抽题数量和每题分数自动计算
 */
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {


    @Autowired
    private ExamRepoService examRepoService;

    /**
     * 保存考试（新增或修改）
     * 【涉及功能：考试详情页标签页 - 核心保存逻辑】
     * 
     * 处理流程（按顺序执行）：
     * 步骤1 - 判断是新增还是修改：
     *   如果 reqDTO 中没有 ID（或为空），说明是新增，用雪花算法生成一个新ID
     *   如果 reqDTO 中有 ID，说明是修改，使用已有的ID
     * 
     * 步骤2 - 计算总分（calcScore 方法）：
     *   遍历题库设置列表，累加每个题库的分数
     *   总分 = 单选数量×单选分数 + 多选数量×多选分数 + 判断数量×判断分数
     *   计算结果直接设置到 reqDTO.totalScore 字段
     * 
     * 步骤3 - 保存考试基本信息：
     *   把 reqDTO 的属性拷贝到 Exam 实体对象
     *   修正状态：如果考试不限时且状态为"进行中"，改为"未开始"
     * 
     * 步骤4 - 保存题库关联（exam_repo 表）：
     *   调用 examRepoService.saveAll(id, repoList)
     *   先删除该考试的所有旧题库关联，再插入新的关联
     *   如果选择了重复的题库，抛出异常
     * 
     * 步骤5 - 保存或更新考试记录：
     *   调用 this.saveOrUpdate(entity)
     *   如果 entity 有 ID 且数据库中存在 → UPDATE
     *   如果 entity 没有 ID 或数据库不存在 → INSERT
     * 
     * @param reqDTO 考试保存请求，包含基本信息+题库设置
     */
    @Override
    public void save(ExamSaveReqDTO reqDTO) {

        // 步骤1：获取考试ID，如果为空则生成新ID（雪花算法）
        String id = reqDTO.getId();

        if(StringUtils.isBlank(id)){
            // 新增考试：用雪花算法生成一个全局唯一的ID
            id = IdWorker.getIdStr();
        }
        // 修改考试：直接使用已有的ID

        // 创建考试实体对象
        Exam entity = new Exam();

        // 步骤2：根据题库设置计算总分
        this.calcScore(reqDTO);

        // 步骤3：把 DTO 的属性拷贝到实体对象（考试名称、时间、总分、及格线等）
        BeanMapper.copy(reqDTO, entity);
        entity.setId(id);
        // 开放类型默认设为1（完全公开），前端已移除该选项
        entity.setOpenType(1);

        // 步骤3.1：修正状态
        // 如果考试不限时（timeLimit=false）且状态为"进行中"（state=2），改为"未开始"（state=0）
        // 原因：不限时的考试不需要"进行中"状态
        if (reqDTO.getTimeLimit()!=null
                && !reqDTO.getTimeLimit()
                && reqDTO.getState()!=null
                && reqDTO.getState() == 2) {
            entity.setState(0);
        } else {
            entity.setState(reqDTO.getState());
        }

        // 步骤4：保存题库关联（先删后插，保证数据一致性）
        try {
            examRepoService.saveAll(id, reqDTO.getRepoList());
        }catch (DuplicateKeyException e){
            // 如果选择了重复的题库，数据库唯一索引会触发此异常
            throw new ServiceException(1, "不能选择重复的题库！");
        }

        // 步骤5：保存或更新考试基本信息（有ID则更新，无ID则插入）
        this.saveOrUpdate(entity);

    }

    /**
     * 查找考试详情（用于编辑时的回显）
     * 【涉及功能：考试详情页标签页 - 编辑回显】
     *
     * 查询逻辑：
     * 1. 从 exam 表查询考试基本信息（名称、时间、总分、及格线等）
     * 2. 从 exam_repo 表查询该考试关联的题库列表（含抽题配置）
     * 3. 把两部分数据组装成 ExamSaveReqDTO 返回
     * 
     * 前端收到这个 DTO 后，分别填充到两个标签页：
     * - 基本信息字段 → "基本信息"标签页
     * - repoList → "题库设置"标签页
     * 
     * @param id 考试ID
     * @return 考试完整信息（含题库设置）
     */
    @Override
    public ExamSaveReqDTO findDetail(String id) {
        // 创建返回对象
        ExamSaveReqDTO respDTO = new ExamSaveReqDTO();

        // 步骤1：查询考试基本信息
        Exam exam = this.getById(id);
        BeanMapper.copy(exam, respDTO);

        // 步骤2：查询该考试关联的题库列表（含每个题库的抽题数量配置）
        List<ExamRepoExtDTO> repos = examRepoService.listByExam(id);
        respDTO.setRepoList(repos);

        return respDTO;
    }

    /**
     * 根据ID查找考试简要信息
     * 只返回考试基本信息，不包含题库和部门关联
     * 
     * @param id 考试ID
     * @return 考试基本信息
     */
    @Override
    public ExamDTO findById(String id) {
        ExamDTO respDTO = new ExamDTO();
        Exam exam = this.getById(id);
        BeanMapper.copy(exam, respDTO);
        return respDTO;
    }

    /**
     * 考试管理分页查询（教师/管理员视角）
     * 
     * @param reqDTO 分页请求
     * @return 分页结果
     */
    @Override
    public IPage<ExamDTO> paging(PagingReqDTO<ExamDTO> reqDTO) {
        // 创建 MyBatis-Plus 分页对象
        Page page = new Page(reqDTO.getCurrent(), reqDTO.getSize());

        // 调用 Mapper 的分页查询方法（具体SQL在 ExamMapper.xml 中）
        IPage<ExamDTO> pageData = baseMapper.paging(page, reqDTO.getParams());
        return pageData;
     }

    /**
     * 在线考试分页查询（学生视角）
     * 只显示当前时间范围内、状态正常的考试
     * 
     * @param reqDTO 分页请求
     * @return 分页结果，每条记录包含考试信息和学生的考试状态
     */
    @Override
    public IPage<ExamOnlineRespDTO> onlinePaging(PagingReqDTO<ExamDTO> reqDTO) {
        // 创建分页对象
        Page page = new Page(reqDTO.getCurrent(), reqDTO.getSize());

        // 调用 Mapper 的在线考试分页查询（SQL中会过滤时间范围和状态）
        IPage<ExamOnlineRespDTO> pageData = baseMapper.online(page, reqDTO.getParams());

        return pageData;
    }

    /**
     * 待阅试卷分页查询
     * 查询包含主观题且需要阅卷的考试列表
     * 
     * @param reqDTO 分页请求
     * @return 分页结果，含待阅数量
     */
    @Override
    public IPage<ExamReviewRespDTO> reviewPaging(PagingReqDTO<ExamDTO> reqDTO) {
        // 创建分页对象
        Page page = new Page(reqDTO.getCurrent(), reqDTO.getSize());

        // 调用 Mapper 的待阅卷分页查询
        IPage<ExamReviewRespDTO> pageData = baseMapper.reviewPaging(page, reqDTO.getParams());

        return pageData;
    }


    /**
     * 计算考试总分
     * 【涉及功能：考试详情页标签页 - 自动计算总分】
     * 
     * 根据题库设置自动计算考试总分，公式：
     * 总分 = Σ(每个题库的单选数量×单选分数 + 多选数量×多选分数 + 判断数量×判断分数)
     * 
     * 例如：
     * 题库1：单选10题×2分 + 多选5题×4分 + 判断5题×1分 = 20+20+5 = 45分
     * 题库2：单选5题×2分 + 多选0题 + 判断0题 = 10分
     * 总分 = 45 + 10 = 55分
     * 
     * 计算结果直接设置到 reqDTO.totalScore 字段
     * 
     * @param reqDTO 考试保存请求，包含题库设置列表
     */
    private void calcScore(ExamSaveReqDTO reqDTO){

        // 客观题总分（目前只有客观题，主观题暂不支持）
        int objScore = 0;

        // 获取题库设置列表
        List<ExamRepoExtDTO> repoList = reqDTO.getRepoList();

        // 遍历每个题库，累加分数
        for(ExamRepoDTO item: repoList){
            // 单选题分数 = 单选数量 × 每题分数
            if(item.getRadioCount()!=null
                    && item.getRadioCount()>0
                    && item.getRadioScore()!=null
                    && item.getRadioScore()>0){
                objScore+=item.getRadioCount()*item.getRadioScore();
            }

            // 多选题分数 = 多选数量 × 每题分数
            if(item.getMultiCount()!=null
                    && item.getMultiCount()>0
                    && item.getMultiScore()!=null
                    && item.getMultiScore()>0){
                objScore+=item.getMultiCount()*item.getMultiScore();
            }

            // 判断题分数 = 判断数量 × 每题分数
            if(item.getJudgeCount()!=null
                    && item.getJudgeCount()>0
                    && item.getJudgeScore()!=null
                    && item.getJudgeScore()>0){
                objScore+=item.getJudgeCount()*item.getJudgeScore();
            }
        }

        // 设置计算出的总分
        reqDTO.setTotalScore(objScore);
    }

}