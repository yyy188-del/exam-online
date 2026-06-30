package com.yy.exam.modules.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.exam.ability.job.enums.JobGroup;
import com.yy.exam.ability.job.enums.JobPrefix;
import com.yy.exam.ability.job.service.JobService;
import com.yy.exam.core.api.ApiError;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.core.exception.ServiceException;
import com.yy.exam.core.utils.BeanMapper;
import com.yy.exam.core.utils.CronUtils;
import com.yy.exam.modules.exam.dto.ExamDTO;
import com.yy.exam.modules.exam.dto.ExamRepoDTO;
import com.yy.exam.modules.exam.dto.ext.ExamRepoExtDTO;
import com.yy.exam.modules.exam.service.ExamRepoService;
import com.yy.exam.modules.exam.service.ExamService;
import com.yy.exam.modules.paper.dto.PaperDTO;
import com.yy.exam.modules.paper.dto.PaperQuDTO;
import com.yy.exam.modules.paper.dto.ext.PaperQuAnswerExtDTO;
import com.yy.exam.modules.paper.dto.ext.PaperQuDetailDTO;
import com.yy.exam.modules.paper.dto.request.PaperAnswerDTO;
import com.yy.exam.modules.paper.dto.request.PaperListReqDTO;
import com.yy.exam.modules.paper.dto.response.ExamDetailRespDTO;
import com.yy.exam.modules.paper.dto.response.ExamResultRespDTO;
import com.yy.exam.modules.paper.dto.response.PaperListRespDTO;
import com.yy.exam.modules.paper.dto.response.PaperStatsRespDTO;
import com.yy.exam.modules.paper.entity.Paper;
import com.yy.exam.modules.paper.entity.PaperQu;
import com.yy.exam.modules.paper.entity.PaperQuAnswer;
import com.yy.exam.modules.paper.enums.ExamState;
import com.yy.exam.modules.paper.enums.PaperState;
import com.yy.exam.modules.paper.job.BreakExamJob;
import com.yy.exam.modules.paper.mapper.PaperMapper;
import com.yy.exam.modules.paper.service.PaperQuAnswerService;
import com.yy.exam.modules.paper.service.PaperQuService;
import com.yy.exam.modules.paper.service.PaperService;
import com.yy.exam.modules.qu.entity.Qu;
import com.yy.exam.modules.qu.entity.QuAnswer;
import com.yy.exam.modules.qu.enums.QuType;
import com.yy.exam.modules.qu.service.QuAnswerService;
import com.yy.exam.modules.qu.service.QuService;
import com.yy.exam.modules.user.UserUtils;
import com.yy.exam.modules.user.book.service.UserBookService;
import com.yy.exam.modules.user.exam.service.UserExamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
* <p>
* 语言设置 服务实现类
* </p>
*
*/
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {


    @Autowired
    private ExamService examService;

    @Autowired
    private QuService quService;

    @Autowired
    private QuAnswerService quAnswerService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperQuService paperQuService;

    @Autowired
    private PaperQuAnswerService paperQuAnswerService;

    @Autowired
    private UserBookService userBookService;

    @Autowired
    private ExamRepoService examRepoService;

    @Autowired
    private UserExamService userExamService;

    @Autowired
    private JobService jobService;

    /**
     * 展示的选项，ABC这样
     */
    private static List<String> ABC = Arrays.asList(new String[]{
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K","L","M","N","O","P","Q","R","S","T","U","V","W","X"
            ,"Y","Z"
    });





    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createPaper(String userId, String examId) {

        // 校验是否有正在考试的试卷
        QueryWrapper<Paper> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Paper::getUserId, userId)
                .eq(Paper::getState, PaperState.ING);

        int exists = this.count(wrapper);


        if (exists > 0) {
            throw new ServiceException(ApiError.ERROR_20010002);
        }

        // 查找考试
        ExamDTO exam = examService.findById(examId);

        if(exam == null){
            throw new ServiceException(1, "考试不存在！");
        }

        if(!ExamState.ENABLE.equals(exam.getState())){
            throw new ServiceException(1, "考试状态不正确！");
        }

        // 考试题目列表
        List<PaperQu> quList = this.generateByRepo(examId);

        if(CollectionUtils.isEmpty(quList)){
            throw new ServiceException(1, "规则不正确，无对应的考题！");
        }

        //保存试卷内容
        Paper paper = this.savePaper(userId, exam, quList);

        // 强制交卷任务
        String jobName = JobPrefix.BREAK_EXAM + paper.getId();
        jobService.addCronJob(BreakExamJob.class, jobName, CronUtils.dateToCron(paper.getLimitTime()), paper.getId());

        return paper.getId();
    }

    /**
     * 获取试卷详情（考试进行中的试卷详情）
     * 【涉及功能：考试详情页标签页 - 学生考试视角】
     * 
     * 返回试卷的基本信息 + 按题型分类的题目列表
     * 题目按题型分为三个列表：单选题列表、多选题列表、判断题列表
     * 前端根据这三个列表分别在答题页的不同区域展示
     * 
     * @param paperId 试卷ID
     * @return 试卷详情，包含题目列表（按题型分类）
     */
    @Override
    public ExamDetailRespDTO paperDetail(String paperId) {

        // 校验试卷所有权：确保当前登录用户就是要查的试卷的拥有者
        checkPaperOwner(paperId);

        ExamDetailRespDTO respDTO = new ExamDetailRespDTO();

        // 1. 查询试卷基本信息（考试名称、总分、时间等）
        Paper paper = paperService.getById(paperId);
        BeanMapper.copy(paper, respDTO);

        // 2. 查询该试卷的所有题目
        List<PaperQuDTO> list = paperQuService.listByPaper(paperId);

        // 3. 按题型分类：单选题、多选题、判断题
        List<PaperQuDTO> radioList = new ArrayList<>();  // 单选题
        List<PaperQuDTO> multiList = new ArrayList<>();  // 多选题
        List<PaperQuDTO> judgeList = new ArrayList<>();  // 判断题
        for(PaperQuDTO item: list){
            if(QuType.RADIO.equals(item.getQuType())){
                radioList.add(item);    // 题型为1（单选题）
            }
            if(QuType.MULTI.equals(item.getQuType())){
                multiList.add(item);    // 题型为2（多选题）
            }
            if(QuType.JUDGE.equals(item.getQuType())){
                judgeList.add(item);    // 题型为3（判断题）
            }
        }

        // 4. 设置分类后的题目列表
        respDTO.setRadioList(radioList);
        respDTO.setMultiList(multiList);
        respDTO.setJudgeList(judgeList);
        return respDTO;
    }

    /**
     * 获取试卷结果（考试完成后的试卷详情，含得分和正确答案）
     * 【涉及功能：考试详情页标签页 - 成绩查看视角】
     * 
     * 返回试卷基本信息 + 每题详情（含学生答案、正确答案、是否得分）
     * 与 paperDetail 的区别：paperDetail 是考试中用的，不显示答案；
     * paperResult 是考试后用的，显示答案和得分
     * 
     * @param paperId 试卷ID
     * @return 试卷结果，包含每题详情（学生答案、正确答案、得分）
     */
    @Override
    public ExamResultRespDTO paperResult(String paperId) {

        // 校验试卷所有权
        checkPaperOwner(paperId);

        ExamResultRespDTO respDTO = new ExamResultRespDTO();

        // 1. 查询试卷基本信息
        Paper paper = paperService.getById(paperId);
        BeanMapper.copy(paper, respDTO);

        // 2. 查询每题详情（含学生答案和正确答案）
        List<PaperQuDetailDTO> quList = paperQuService.listForPaperResult(paperId);
        respDTO.setQuList(quList);

        return respDTO;
    }

    @Override
    public PaperQuDetailDTO findQuDetail(String paperId, String quId) {

        PaperQuDetailDTO respDTO = new PaperQuDetailDTO();
        // 问题
        Qu qu = quService.getById(quId);

        // 基本信息
        PaperQu paperQu = paperQuService.findByKey(paperId, quId);
        BeanMapper.copy(paperQu, respDTO);
        respDTO.setContent(qu.getContent());
        respDTO.setImage(qu.getImage());

        // 答案列表
        List<PaperQuAnswerExtDTO> list = paperQuAnswerService.listForExam(paperId, quId);
        respDTO.setAnswerList(list);

        return respDTO;
    }


    /**
     * 题库组题方式产生题目列表
     * @param examId
     * @return
     */
    private List<PaperQu> generateByRepo(String examId){

        // 查找规则指定的题库
        List<ExamRepoExtDTO> list = examRepoService.listByExam(examId);

        //最终的题目列表
        List<PaperQu> quList = new ArrayList<>();

        //排除ID，避免题目重复
        List<String> excludes = new ArrayList<>();
        excludes.add("none");

        if (!CollectionUtils.isEmpty(list)) {
            for (ExamRepoExtDTO item : list) {

                // 单选题
                if(item.getRadioCount() > 0){
                    List<Qu> radioList = quService.listByRandom(item.getRepoId(), QuType.RADIO, excludes, item.getRadioCount());
                    for (Qu qu : radioList) {
                        PaperQu paperQu = this.processPaperQu(item, qu);
                        quList.add(paperQu);
                        excludes.add(qu.getId());
                    }
                }

                //多选题
                if(item.getMultiCount() > 0) {
                    List<Qu> multiList = quService.listByRandom(item.getRepoId(), QuType.MULTI, excludes,
                            item.getMultiCount());
                    for (Qu qu : multiList) {
                        PaperQu paperQu = this.processPaperQu(item, qu);
                        quList.add(paperQu);
                        excludes.add(qu.getId());
                    }
                }

                // 判断题
                if(item.getJudgeCount() > 0) {
                    List<Qu> judgeList = quService.listByRandom(item.getRepoId(), QuType.JUDGE, excludes,
                            item.getJudgeCount());
                    for (Qu qu : judgeList) {
                        PaperQu paperQu = this.processPaperQu(item, qu);
                        quList.add(paperQu);
                        excludes.add(qu.getId());
                    }
                }
            }
        }
        return quList;
    }



    /**
     * 填充试题题目信息
     * @param repo
     * @param qu
     * @return
     */
    private PaperQu processPaperQu(ExamRepoDTO repo, Qu qu) {

        //保存试题信息
        PaperQu paperQu = new PaperQu();
        paperQu.setQuId(qu.getId());
        paperQu.setAnswered(false);
        paperQu.setIsRight(false);
        paperQu.setQuType(qu.getQuType());

        if (QuType.RADIO.equals(qu.getQuType())) {
            paperQu.setScore(repo.getRadioScore());
            paperQu.setActualScore(repo.getRadioScore());
        }

        if (QuType.MULTI.equals(qu.getQuType())) {
            paperQu.setScore(repo.getMultiScore());
            paperQu.setActualScore(repo.getMultiScore());
        }

        if (QuType.JUDGE.equals(qu.getQuType())) {
            paperQu.setScore(repo.getJudgeScore());
            paperQu.setActualScore(repo.getJudgeScore());
        }

        return paperQu;
    }


    /**
     * 保存试卷
     * @param userId
     * @param exam
     * @param quList
     * @return
     */
    private Paper savePaper(String userId, ExamDTO exam, List<PaperQu> quList) {

        //保存试卷基本信息
        Paper paper = new Paper();
        paper.setExamId(exam.getId());
        paper.setTitle(exam.getTitle());
        paper.setTotalScore(exam.getTotalScore());
        paper.setTotalTime(exam.getTotalTime());
        paper.setUserScore(0);
        paper.setUserId(userId);
        paper.setCreateTime(new Date());
        paper.setUpdateTime(new Date());
        paper.setQualifyScore(exam.getQualifyScore());
        paper.setState(PaperState.ING);
        paper.setHasSaq(false);

        // 截止时间
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(System.currentTimeMillis());
        cl.add(Calendar.MINUTE, exam.getTotalTime());
        paper.setLimitTime(cl.getTime());

        paperService.save(paper);

        if (!CollectionUtils.isEmpty(quList)) {
            this.savePaperQu(paper.getId(), quList);
        }

        return paper;
    }



    /**
     * 保存试卷试题列表
     * @param paperId
     * @param quList
     */
    private void savePaperQu(String paperId, List<PaperQu> quList){

        List<PaperQu> batchQuList = new ArrayList<>();
        List<PaperQuAnswer> batchAnswerList = new ArrayList<>();

        int sort = 0;
        for (PaperQu item : quList) {

            item.setPaperId(paperId);
            item.setSort(sort);
            item.setId(IdWorker.getIdStr());

            //回答列表
            List<QuAnswer> answerList = quAnswerService.listAnswerByRandom(item.getQuId());

            if (!CollectionUtils.isEmpty(answerList)) {

                int ii = 0;
                for (QuAnswer answer : answerList) {
                    PaperQuAnswer paperQuAnswer = new PaperQuAnswer();
                    paperQuAnswer.setId(UUID.randomUUID().toString());
                    paperQuAnswer.setPaperId(paperId);
                    paperQuAnswer.setQuId(answer.getQuId());
                    paperQuAnswer.setAnswerId(answer.getId());
                    paperQuAnswer.setChecked(false);
                    paperQuAnswer.setSort(ii);
                    paperQuAnswer.setAbc(ABC.get(ii));
                    paperQuAnswer.setIsRight(answer.getIsRight());
                    ii++;
                    batchAnswerList.add(paperQuAnswer);
                }
            }

            batchQuList.add(item);
            sort++;
        }

        //添加问题
        paperQuService.saveBatch(batchQuList);

        //批量添加问题答案
        paperQuAnswerService.saveBatch(batchAnswerList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void fillAnswer(PaperAnswerDTO reqDTO) {

        // 校验试卷所有权
        checkPaperOwner(reqDTO.getPaperId());

        // 未作答
        if(CollectionUtils.isEmpty(reqDTO.getAnswers())
                && StringUtils.isBlank(reqDTO.getAnswer())){
            return;
        }

        //查找答案列表
        List<PaperQuAnswer> list = paperQuAnswerService.listForFill(reqDTO.getPaperId(), reqDTO.getQuId());

        //是否正确
        boolean right = true;

        //更新正确答案
        for (PaperQuAnswer item : list) {

            if (reqDTO.getAnswers().contains(item.getId())) {
                item.setChecked(true);
            } else {
                item.setChecked(false);
            }

            //有一个对不上就是错的
            if (item.getIsRight()!=null && !item.getIsRight().equals(item.getChecked())) {
                right = false;
            }
            paperQuAnswerService.updateById(item);
        }

        //修改为已回答
        PaperQu qu = new PaperQu();
        qu.setQuId(reqDTO.getQuId());
        qu.setPaperId(reqDTO.getPaperId());
        qu.setIsRight(right);
        qu.setAnswer(reqDTO.getAnswer());
        qu.setAnswered(true);

        paperQuService.updateByKey(qu);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handExam(String paperId) {

        // 校验试卷所有权
        checkPaperOwner(paperId);

        //获取试卷信息
        Paper paper = paperService.getById(paperId);

        //如果不是正常的，抛出异常
        if(!PaperState.ING.equals(paper.getState())){
            throw new ServiceException(1, "试卷状态不正确！");
        }

        // 客观分
        int objScore = paperQuService.sumObjective(paperId);
        paper.setObjScore(objScore);
        paper.setUserScore(objScore);

        // 主观分，因为要阅卷，所以给0
        paper.setSubjScore(0);

        // 待阅卷
        if(paper.getHasSaq()) {
            paper.setState(PaperState.WAIT_OPT);
        }else {

            // 同步保存考试成绩
            userExamService.joinResult(paper.getUserId(), paper.getExamId(), objScore, objScore>=paper.getQualifyScore());

            paper.setState(PaperState.FINISHED);
        }
        paper.setUpdateTime(new Date());

        //计算考试时长
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(System.currentTimeMillis());
        int userTime = (int)((System.currentTimeMillis() - paper.getCreateTime().getTime()) / 1000 / 60);
        if(userTime == 0){
            userTime = 1;
        }
        paper.setUserTime(userTime);

        //更新试卷
        paperService.updateById(paper);


        // 终止定时任务
        String name = JobPrefix.BREAK_EXAM + paperId;
        jobService.deleteJob(name, JobGroup.SYSTEM);

        //把打错的问题加入错题本
        List<PaperQuDTO> list = paperQuService.listByPaper(paperId);
        for(PaperQuDTO qu: list){
            // 主观题和对的都不加入错题库
            if(qu.getIsRight()){
                continue;
            }
            //加入错题本
            new Thread(() -> userBookService.addBook(paper.getExamId(), qu.getQuId())).run();
        }
    }

    /**
     * 分页查询试卷列表（成绩列表）
     * 【涉及功能：分数排序、仅看不及格筛选】
     * 
     * 这个方法直接调用 Mapper 层的 paging 方法，把参数透传给 MyBatis XML
     * 不做任何业务逻辑处理，所有筛选和排序都在 XML 中通过动态 SQL 实现
     * 
     * @param reqDTO 分页请求，包含分页参数和查询条件（PaperListReqDTO）
     * @return 分页结果
     */
    @Override
    public IPage<PaperListRespDTO> paging(PagingReqDTO<PaperListReqDTO> reqDTO) {
        // reqDTO.toPage()：把分页参数（current, size）转为 MyBatis-Plus 的 Page 对象
        // reqDTO.getParams()：获取查询条件（考试ID、排序字段、是否仅看不及格等）
        // 具体SQL逻辑在 PaperMapper.xml 的 paging 查询中
        return baseMapper.paging(reqDTO.toPage(), reqDTO.getParams());
    }


    @Override
    public PaperDTO checkProcess(String userId) {

        QueryWrapper<Paper> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Paper::getUserId, userId)
                .eq(Paper::getState, PaperState.ING);

        Paper paper = this.getOne(wrapper, false);

        if (paper != null) {
            return BeanMapper.map(paper, PaperDTO.class);
        }

        return null;
    }

    /**
     * 校验试卷所有权
     * @param paperId
     */
    private void checkPaperOwner(String paperId) {
        Paper paper = paperService.getById(paperId);
        if (paper == null || !paper.getUserId().equals(UserUtils.getUserId())) {
            throw new ServiceException(1, "无权操作此试卷！");
        }
    }

    /**
     * 成绩统计
     * 【新增功能：成绩分析弹窗】
     * 
     * 统计某次考试的成绩数据，包括：
     * 1. 总人次（参加考试的人数）
     * 2. 及格/不及格人数（与及格线比较）
     * 3. 平均分、最高分、最低分
     * 4. 分数段分布（0-59, 60-69, 70-79, 80-89, 90-100）
     * 
     * 核心算法：
     * - 查该考试的所有试卷 → 遍历每条试卷 → 累加统计
     * - 只统计已交卷（state=FINISHED）且有分数的试卷
     * - 平均分 = 总分 / 已完成人数，保留一位小数
     * 
     * @param examId 考试ID
     * @return 成绩统计数据
     */
    @Override
    public PaperStatsRespDTO stats(String examId) {
        // 1. 创建返回对象
        PaperStatsRespDTO resp = new PaperStatsRespDTO();

        // 2. 查询该考试的所有试卷
        QueryWrapper<Paper> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Paper::getExamId, examId);  // WHERE exam_id = ?
        List<Paper> papers = this.list(wrapper);         // 执行查询

        // 3. 设置总人次（所有试卷数，含未交卷的）
        resp.setTotalPapers(papers.size());

        // 4. 定义统计变量
        int passCount = 0;      // 及格人数
        int failCount = 0;      // 不及格人数
        int totalScore = 0;     // 总分（用于计算平均分）
        int maxScore = 0;       // 最高分
        int minScore = Integer.MAX_VALUE;  // 最低分，初始值设为最大值，方便比较

        // 5. 分数段分布数组，5个段：0-59, 60-69, 70-79, 80-89, 90-100
        int[] distribution = new int[5];

        // 6. 遍历每张试卷，累加统计
        for (Paper paper : papers) {
            // 只统计已交卷且有分数的试卷（没交卷的试卷没有分数，不参与统计）
            if (paper.getState() != null && paper.getState() == PaperState.FINISHED
                    && paper.getUserScore() != null && paper.getQualifyScore() != null) {
                int score = paper.getUserScore();      // 学生实际得分
                int qualify = paper.getQualifyScore(); // 该考试的及格线

                // 累计总分
                totalScore += score;

                // 更新最高分：如果当前分数 > 历史最高分，则更新
                if (score > maxScore) maxScore = score;

                // 更新最低分：如果当前分数 < 历史最低分，则更新
                if (score < minScore) minScore = score;

                // 判断是否及格：分数 >= 及格线
                if (score >= qualify) {
                    passCount++;   // 及格
                } else {
                    failCount++;   // 不及格
                }

                // 判断属于哪个分数段
                if (score < 60) {
                    distribution[0]++;     // 0-59分
                } else if (score < 70) {
                    distribution[1]++;     // 60-69分
                } else if (score < 80) {
                    distribution[2]++;     // 70-79分
                } else if (score < 90) {
                    distribution[3]++;     // 80-89分
                } else {
                    distribution[4]++;     // 90-100分
                }
            }
        }

        // 7. 设置统计结果
        resp.setPassCount(passCount);
        resp.setFailCount(failCount);

        // 8. 计算平均分（总分 / 已完成人数，保留一位小数）
        int finishedCount = passCount + failCount;  // 已完成人数 = 及格 + 不及格
        if (finishedCount > 0) {
            // 计算技巧：先除再乘10取整再除10，实现保留一位小数
            // 例如：725/10=72.5 → Math.round(72.5)=73 → 73/10.0=7.3
            resp.setAvgScore((double) Math.round((float) totalScore / finishedCount * 10) / 10);
        } else {
            resp.setAvgScore(0.0);  // 没有人完成考试，平均分就是0
        }
        resp.setMaxScore(maxScore);
        // 如果 minScore 还是初始值，说明没有已完成考试的人，设为0
        resp.setMinScore(minScore == Integer.MAX_VALUE ? 0 : minScore);

        // 9. 把分数段数组转成对象列表，方便前端循环展示
        List<PaperStatsRespDTO.ScoreRange> distList = new ArrayList<>();
        String[] labels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        for (int i = 0; i < distribution.length; i++) {
            PaperStatsRespDTO.ScoreRange item = new PaperStatsRespDTO.ScoreRange();
            item.setLabel(labels[i]);       // 分数段名称
            item.setCount(distribution[i]); // 该分数段人数
            distList.add(item);
        }
        resp.setDistribution(distList);

        return resp;
    }
}