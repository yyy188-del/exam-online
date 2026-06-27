<template>
  <div class="dashboard-container">
    <div class="stats-row">
      <div class="stat-item" v-for="card in statCards" :key="card.key">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
          <div class="stat-icon" :style="{ background: card.color }">
            <i :class="card.icon" />
          </div>
        </div>
        <div class="stat-footer">{{ card.desc }}</div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-upload" style="margin-right: 8px; color: #1890ff" />
            <span>试题导入</span>
          </div>
          <div class="import-section">
            <div class="import-desc">
              支持 Excel 批量导入试题，请先下载模板并按格式填写后上传
            </div>
            <el-button type="primary" icon="el-icon-download" size="small" @click="downloadQuTemplate">
              下载试题模板
            </el-button>
            <el-upload
              :show-file-list="false"
              :http-request="uploadQu"
              accept=".xlsx,.xls"
              style="display: inline-block; margin-left: 12px"
            >
              <el-button type="success" icon="el-icon-upload2" size="small">
                导入试题
              </el-button>
            </el-upload>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-user" style="margin-right: 8px; color: #52c41a" />
            <span>用户导入</span>
          </div>
          <div class="import-section">
            <div class="import-desc">
              支持 Excel 批量导入用户，请先下载模板并按格式填写后上传
            </div>
            <el-button type="primary" icon="el-icon-download" size="small" @click="downloadUserTemplate">
              下载用户模板
            </el-button>
            <el-upload
              :show-file-list="false"
              :http-request="uploadUser"
              accept=".xlsx,.xls"
              style="display: inline-block; margin-left: 12px"
            >
              <el-button type="success" icon="el-icon-upload2" size="small">
                导入用户
              </el-button>
            </el-upload>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-s-operation" style="margin-right: 8px; color: #722ed1" />
            <span>快捷操作</span>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/exam/qu/add')">
              <div class="action-icon" style="background: #e6f7ff">
                <i class="el-icon-edit-outline" style="color: #1890ff" />
              </div>
              <div class="action-info">
                <div class="action-name">添加试题</div>
                <div class="action-desc">手动添加一道新试题</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/exam/exam/add')">
              <div class="action-icon" style="background: #f6ffed">
                <i class="el-icon-plus" style="color: #52c41a" />
              </div>
              <div class="action-info">
                <div class="action-name">创建考试</div>
                <div class="action-desc">配置新的考试场次</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/sys/user')">
              <div class="action-icon" style="background: #fff7e6">
                <i class="el-icon-user" style="color: #faad14" />
              </div>
              <div class="action-info">
                <div class="action-name">用户管理</div>
                <div class="action-desc">管理系统用户和角色</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/exam/repo/add')">
              <div class="action-icon" style="background: #f9f0ff">
                <i class="el-icon-collection" style="color: #722ed1" />
              </div>
              <div class="action-info">
                <div class="action-name">添加题库</div>
                <div class="action-desc">创建新的试题分类</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-info" style="margin-right: 8px; color: #1890ff" />
            <span>使用提示</span>
          </div>
          <div class="tips-box">
            <div class="tips-list">
              <p>1. 先创建题库，再导入试题</p>
              <p>2. 创建考试时关联题库抽题</p>
              <p>3. 支持单选、多选、判断题</p>
              <p>4. 可对用户进行角色分配</p>
              <p>5. 系统配置可修改站点信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { post } from '@/utils/request'
import { download, upload } from '@/utils/request'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      stats: {
        examCount: 0,
        repoCount: 0,
        userCount: 0,
        paperCount: 0,
        quCount: 0
      }
    }
  },
  computed: {
    statCards() {
      return [
        {
          key: 'repo',
          title: '题库数量',
          value: this.stats.repoCount,
          icon: 'el-icon-collection',
          color: '#1890ff',
          desc: '管理试题分类题库'
        },
        {
          key: 'qu',
          title: '试题数量',
          value: this.stats.quCount,
          icon: 'el-icon-edit-outline',
          color: '#722ed1',
          desc: '系统中的试题总数'
        },
        {
          key: 'user',
          title: '用户数量',
          value: this.stats.userCount,
          icon: 'el-icon-user',
          color: '#52c41a',
          desc: '系统注册用户总数'
        },
        {
          key: 'exam',
          title: '考试场次',
          value: this.stats.examCount,
          icon: 'el-icon-data-line',
          color: '#faad14',
          desc: '创建的考试总场次'
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
        console.log('load stats error')
      }
    },
    downloadQuTemplate() {
      download('/exam/api/qu/qu/import/template', {}, '试题导入模板.xlsx')
    },
    async uploadQu(file) {
      try {
        const res = await upload('/exam/api/qu/qu/import', file.file)
        if (res.code === 0) {
          this.$message.success('试题导入成功')
          this.loadStats()
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (e) {
        this.$message.error('导入失败，请检查文件格式')
      }
    },
    downloadUserTemplate() {
      download('/exam/api/sys/user/import/template', {}, '用户导入模板.xlsx')
    },
    async uploadUser(file) {
      try {
        const res = await upload('/exam/api/sys/user/import', file.file)
        if (res.code === 0) {
          this.$message.success(res.data || '用户导入成功')
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
.tips-list {
  font-size: 13px;
  color: #666;
  line-height: 2;
}
</style>

