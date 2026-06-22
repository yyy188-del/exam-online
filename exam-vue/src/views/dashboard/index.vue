<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-item" v-for="item in statCards" :key="item.key">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-title">{{ item.title }}</div>
          </div>
          <div class="stat-icon" :style="{ background: item.color }">
            <i :class="item.icon" />
          </div>
        </div>
        <div class="stat-footer">
          <span>{{ item.desc }}</span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧：数据导入 -->
      <el-col :span="14">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-header">
            <i class="el-icon-upload2" style="color:#1890ff;font-size:18px;margin-right:8px;" />
            <span>一键导入</span>
          </div>

          <el-tabs v-model="importTab" type="card">
            <!-- 题库导入 -->
            <el-tab-pane label="题库导入" name="repo">
              <div class="import-section">
                <div class="import-desc">
                  <i class="el-icon-info" style="color:#1890ff;" />
                  下载Excel模板，按格式填写试题后上传，支持单选、多选、判断题批量导入
                </div>
                <div class="import-actions">
                  <el-button type="primary" icon="el-icon-download" @click="downloadQuTemplate">
                    下载试题模板
                  </el-button>
                  <el-upload
                    :show-file-list="false"
                    :http-request="uploadQu"
                    accept=".xls,.xlsx"
                    style="display: inline-block; margin-left: 12px;"
                  >
                    <el-button type="success" icon="el-icon-upload2">
                      上传试题文件
                    </el-button>
                  </el-upload>
                </div>
                <div class="import-steps">
                  <el-steps :active="0" align-center>
                    <el-step title="下载模板" description="按模板填写试题" />
                    <el-step title="上传文件" description="选择Excel文件导入" />
                    <el-step title="完成导入" description="试题自动加入题库" />
                  </el-steps>
                </div>
              </div>
            </el-tab-pane>

            <!-- 学员导入 -->
            <el-tab-pane label="学员导入" name="user">
              <div class="import-section">
                <div class="import-desc">
                  <i class="el-icon-info" style="color:#52c41a;" />
                  下载Excel模板，填写学员信息后上传，支持批量创建账号。角色可填：student(学员)、teacher(教师)、sa(管理员)
                </div>
                <div class="import-actions">
                  <el-button type="primary" icon="el-icon-download" @click="downloadUserTemplate">
                    下载学员模板
                  </el-button>
                  <el-upload
                    :show-file-list="false"
                    :http-request="uploadUser"
                    accept=".xls,.xlsx"
                    style="display: inline-block; margin-left: 12px;"
                  >
                    <el-button type="success" icon="el-icon-upload2">
                      上传学员文件
                    </el-button>
                  </el-upload>
                </div>
                <div class="import-steps">
                  <el-steps :active="0" align-center>
                    <el-step title="下载模板" description="按模板填写学员信息" />
                    <el-step title="上传文件" description="选择Excel文件导入" />
                    <el-step title="完成导入" description="账号自动创建完成" />
                  </el-steps>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- 右侧：快捷操作 + 使用提示 -->
      <el-col :span="10">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-header">
            <i class="el-icon-s-operation" style="color:#13c2c2;font-size:18px;margin-right:8px;" />
            <span>快捷操作</span>
          </div>

          <div class="quick-actions">
            <div class="action-item" @click="$router.push({ name: 'AddExam' })">
              <div class="action-icon" style="background: #e6f7ff; color: #1890ff;">
                <i class="el-icon-edit-outline" />
              </div>
              <div class="action-info">
                <div class="action-name">创建考试</div>
                <div class="action-desc">设置考试规则与范围</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push({ name: 'AddQu' })">
              <div class="action-icon" style="background: #f6ffed; color: #52c41a;">
                <i class="el-icon-document-add" />
              </div>
              <div class="action-info">
                <div class="action-name">添加试题</div>
                <div class="action-desc">手动录入或批量导入</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push({ name: 'AddRepo' })">
              <div class="action-icon" style="background: #fff7e6; color: #faad14;">
                <i class="el-icon-folder-add" />
              </div>
              <div class="action-info">
                <div class="action-name">新建题库</div>
                <div class="action-desc">创建分类题库</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push({ name: 'SysUser' })">
              <div class="action-icon" style="background: #f0f5ff; color: #722ed1;">
                <i class="el-icon-user" />
              </div>
              <div class="action-info">
                <div class="action-name">用户管理</div>
                <div class="action-desc">管理账号与权限</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
          </div>

          <div class="tips-box">
            <div class="tips-title">
              <i class="el-icon-guide" /> 使用提示
            </div>
            <div class="tips-list">
              <div>1. 先在「题库管理」中创建题库</div>
              <div>2. 在「试题管理」中添加试题</div>
              <div>3. 在「后台管理」中创建考试并关联题库</div>
              <div>4. 学员即可在「我的考试」中参加考试</div>
            </div>
          </div>

          <div style="text-align:center; color:#bbb; font-size:12px; margin-top:16px;">
            版本 {{ version }}
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { post, upload } from '@/utils/request'
import { download } from '@/utils/request'
import config from '../../../package.json'

export default {
  name: 'Dashboard',
  data() {
    return {
      version: config.version,
      importTab: 'repo',
      stats: {
        examCount: 0,
        repoCount: 0,
        userCount: 0,
        paperCount: 0
      }
    }
  },
  computed: {
    statCards() {
      return [
        {
          key: 'exam',
          title: '考试总数',
          value: this.stats.examCount,
          icon: 'el-icon-document',
          color: '#13c2c2',
          desc: '已创建的考试'
        },
        {
          key: 'repo',
          title: '题库总数',
          value: this.stats.repoCount,
          icon: 'el-icon-collection',
          color: '#1890ff',
          desc: '已创建的题库'
        },
        {
          key: 'user',
          title: '用户总数',
          value: this.stats.userCount,
          icon: 'el-icon-user',
          color: '#52c41a',
          desc: '系统注册用户'
        },
        {
          key: 'paper',
          title: '考试记录',
          value: this.stats.paperCount,
          icon: 'el-icon-data-line',
          color: '#faad14',
          desc: '已提交的试卷'
        }
      ]
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const res = await post('/exam/api/sys/dashboard/stats', {})
        if (res.code === 0) {
          this.stats = res.data
        }
      } catch (e) {
        console.log('统计数据加载失败，使用默认值')
      }
    },

    downloadQuTemplate() {
      download('/exam/api/qu/qu/import/template', {}, '试题导入模板.xlsx')
    },

    async uploadQu(file) {
      try {
        const res = await upload('/exam/api/qu/qu/import', file.file)
        if (res.code === 0) {
          this.$message.success('试题导入成功！')
          this.loadStats()
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (e) {
        this.$message.error('导入失败，请检查文件格式')
      }
    },

    downloadUserTemplate() {
      download('/exam/api/sys/user/import/template', {}, '学员导入模板.xlsx')
    },

    async uploadUser(file) {
      try {
        const res = await upload('/exam/api/sys/user/import', file.file)
        if (res.code === 0) {
          this.$message.success(res.data || '学员导入成功！')
          this.loadStats()
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (e) {
        this.$message.error('导入失败，请检查文件格式')
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 12px;
}

.stat-left {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}

.stat-title {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-footer {
  border-top: 1px solid #f0f0f0;
  padding: 8px 24px;
  font-size: 12px;
  color: #bbb;
}

.panel-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.panel-card >>> .el-card__header {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-card >>> .el-card__body {
  padding: 0;
}

.panel-header {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}

.import-section {
  padding: 20px;
}

.import-desc {
  background: #f6f8fb;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 16px;
}

.import-actions {
  margin-bottom: 24px;
}

.import-steps {
  margin-top: 8px;
  padding: 0 10px;
}

.quick-actions {
  padding: 8px 20px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.action-item:hover {
  background: #f5f7fa;
}

.action-item:last-child {
  margin-bottom: 0;
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.action-info {
  flex: 1;
  margin-left: 14px;
}

.action-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.action-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.action-arrow {
  color: #ccc;
  font-size: 14px;
}

.tips-box {
  margin: 0 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f6f9fc 0%, #f0f5ff 100%);
  border-radius: 8px;
  border: 1px solid #e8f0fe;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 12px;
}

.tips-list {
  font-size: 13px;
  color: #666;
  line-height: 2;
}
</style>
