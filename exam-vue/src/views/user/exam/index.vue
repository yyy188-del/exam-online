<template>

  <div>
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="考试人员" name="users">
        <data-table
          ref="pagingTable"
          :options="userOptions"
          :list-query="userQuery"
        >
          <template #filter-content>
            <el-input v-model="userQuery.params.realName" placeholder="搜索人员" style="width: 200px;" class="filter-item" />
          </template>

          <template #data-columns>
            <el-table-column label="人员" prop="realName" align="center" />
            <el-table-column label="考试次数" prop="tryCount" align="center" />
            <el-table-column label="最高分" prop="maxScore" align="center" />
            <el-table-column label="是否通过" align="center">
              <template v-slot="scope">
                <span v-if="scope.row.passed" style="color: #00ff00;">通过</span>
                <span v-else style="color: #ff0000;">未通过</span>
              </template>
            </el-table-column>
            <el-table-column label="最后考试时间" prop="updateTime" align="center" />
            <el-table-column label="操作" align="center">
              <template v-slot="scope">
                <el-button type="primary" size="mini" icon="el-icon-view" @click="handleExamDetail(scope.row.userId)">考试明细</el-button>
              </template>
            </el-table-column>
          </template>
        </data-table>
      </el-tab-pane>

      <el-tab-pane label="考试记录" name="papers">
        <data-table
          ref="paperTable"
          :options="paperOptions"
          :list-query="paperQuery"
        >
          <template #filter-content>
            <el-select v-model="paperQuery.params.state" placeholder="考试状态" class="filter-item" clearable>
              <el-option v-for="item in paperStates" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>

            <el-button
              :type="paperQuery.params.passFilter === 1 ? 'danger' : ''"
              class="filter-item"
              icon="el-icon-s-data"
              @click="togglePassFilter"
            >
              {{ paperQuery.params.passFilter === 1 ? '取消筛选不及格' : '仅看不及格' }}
            </el-button>

            <el-dropdown class="filter-item" @command="handleSort">
              <el-button :type="sortLabel ? 'primary' : ''" plain>
                {{ sortLabel || '排序方式' }}<i class="el-icon-arrow-down el-icon--right" />
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="score-desc">分数从高到低</el-dropdown-item>
                <el-dropdown-item command="score-asc">分数从低到高</el-dropdown-item>
                <el-dropdown-item command="time">按考试时间</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <el-button
              type="success"
              class="filter-item"
              icon="el-icon-data-analysis"
              @click="openStats"
            >
              成绩分析
            </el-button>
          </template>

          <template #data-columns>
            <el-table-column label="考试名称" align="center" prop="title" show-overflow-tooltip />
            <el-table-column label="人员" align="center" prop="realName" />
            <el-table-column label="考试时长(分钟)" align="center" prop="totalTime">
              <template v-slot="scope">
                {{ scope.row.userTime }} / {{ scope.row.totalTime }}
              </template>
            </el-table-column>
            <el-table-column label="考试得分" align="center" sortable="custom" :sort-orders="['ascending', 'descending']">
              <template v-slot="scope">
                <span :class="getScoreClass(scope.row)">
                  {{ scope.row.userScore }} / {{ scope.row.totalScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="考试时间" align="center" prop="createTime" width="180px" />
            <el-table-column label="考试结果" align="center">
              <template v-slot="scope">
                <span v-if="scope.row.state===1">待阅卷</span>
                <span v-else-if="scope.row.state===0">待交卷</span>
                <span v-else>
                  <span v-if="scope.row.userScore >= scope.row.qualifyScore" style="color:#52c41a;font-weight:bold;">合格</span>
                  <span v-else style="color: #ff4d4f;font-weight:bold;">不合格</span>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="考试状态" align="center">
              <template v-slot="scope">
                {{ scope.row.state | paperStateFilter }}
              </template>
            </el-table-column>
            </template>
        </data-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :visible.sync="dialogVisible" title="考试明细" width="60%">
      <div class="el-dialog-div">
        <my-paper-list :exam-id="examId" :user-id="userId" />
      </div>
    </el-dialog>

    <el-dialog :visible.sync="statsVisible" title="成绩分析" width="700px" :close-on-click-modal="false">
      <div v-if="statsData" class="stats-container">
        <div class="stats-summary">
          <div class="summary-item">
            <div class="summary-value">{{ statsData.totalPapers }}</div>
            <div class="summary-label">总人次</div>
          </div>
          <div class="summary-item pass">
            <div class="summary-value">{{ statsData.passCount }}</div>
            <div class="summary-label">及格</div>
          </div>
          <div class="summary-item fail">
            <div class="summary-value">{{ statsData.failCount }}</div>
            <div class="summary-label">不及格</div>
          </div>
          <div class="summary-item">
            <div class="summary-value">{{ statsData.avgScore }}</div>
            <div class="summary-label">平均分</div>
          </div>
          <div class="summary-item">
            <div class="summary-value">{{ statsData.maxScore }}</div>
            <div class="summary-label">最高分</div>
          </div>
          <div class="summary-item">
            <div class="summary-value">{{ statsData.minScore }}</div>
            <div class="summary-label">最低分</div>
          </div>
        </div>

        <div class="stats-chart">
          <div class="chart-title">分数段分布</div>
          <div class="bar-chart">
            <div v-for="item in statsData.distribution" :key="item.label" class="bar-row">
              <div class="bar-label">{{ item.label }}</div>
              <div class="bar-track">
                <div
                  class="bar-fill"
                  :style="{
                    width: maxCount > 0 ? (item.count / maxCount * 100) + '%' : '0%',
                    background: getBarColor(item.label)
                  }"
                >
                  <span class="bar-count">{{ item.count }}人</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="pass-pie">
          <div class="chart-title">及格率</div>
          <div class="pie-visual">
            <div class="pie-ring">
              <svg viewBox="0 0 120 120" width="140" height="140">
                <circle cx="60" cy="60" r="54" fill="none" stroke="#f0f0f0" stroke-width="12" />
                <circle
                  cx="60" cy="60" r="54"
                  fill="none"
                  stroke="#52c41a"
                  stroke-width="12"
                  stroke-linecap="round"
                  :stroke-dasharray="passPercent * 3.393 + ' ' + (339.3 - passPercent * 3.393)"
                  transform="rotate(-90 60 60)"
                />
              </svg>
              <div class="pie-center">
                <div class="pie-value">{{ passPercent }}%</div>
                <div class="pie-label">通过率</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else style="text-align:center;padding:40px;color:#999;">
        <i class="el-icon-loading" /> 加载中...
      </div>
    </el-dialog>
  </div>

</template>

<script>
import DataTable from '@/components/DataTable'
import MyPaperList from './paper'
import { paperStats } from '@/api/paper/paper'

export default {
  name: 'ExamUserList',
  components: { MyPaperList, DataTable },
  data() {
    return {
      activeTab: 'users',
      dialogVisible: false,
      statsVisible: false,
      statsData: null,
      sortLabel: '',
      examId: '',
      userId: '',

      paperStates: [
        { value: 0, label: '考试中' },
        { value: 1, label: '待阅卷' },
        { value: 2, label: '已考完' },
        { value: 3, label: '!已弃考' }
      ],

      userQuery: {
        current: 1,
        size: 10,
        params: {
          examId: '',
          realName: ''
        }
      },

      userOptions: {
        listUrl: '/exam/api/user/exam/paging',
        multi: false
      },

      paperQuery: {
        current: 1,
        size: 10,
        params: {
          examId: '',
          passFilter: 0
        }
      },

      paperOptions: {
        multi: false,
        listUrl: '/exam/api/paper/paper/paging'
      }
    }
  },

  computed: {
    passPercent() {
      if (!this.statsData || this.statsData.totalPapers === 0) return 0
      return Math.round(this.statsData.passCount / this.statsData.totalPapers * 100)
    },
    maxCount() {
      if (!this.statsData || !this.statsData.distribution) return 0
      let max = 0
      this.statsData.distribution.forEach(item => {
        if (item.count > max) max = item.count
      })
      return max
    }
  },

  created() {
    const examId = this.$route.params.examId
    if (typeof examId !== 'undefined') {
      this.examId = examId
      this.userQuery.params.examId = examId
      this.paperQuery.params.examId = examId
    }
  },

  methods: {

    handleExamDetail(userId) {
      this.userId = userId
      this.dialogVisible = true
    },
    togglePassFilter() {
      if (this.paperQuery.params.passFilter === 1) {
        this.paperQuery.params.passFilter = 0
      } else {
        this.paperQuery.params.passFilter = 1
      }
    },

    handleSort(command) {
      if (command === 'score-desc') {
        this.sortLabel = '分数从高到低'
        this.paperQuery.params.sortField = 'userScore'
        this.paperQuery.params.sortOrder = 'DESC'
      } else if (command === 'score-asc') {
        this.sortLabel = '分数从低到高'
        this.paperQuery.params.sortField = 'userScore'
        this.paperQuery.params.sortOrder = 'ASC'
      } else {
        this.sortLabel = ''
        this.paperQuery.params.sortField = ''
        this.paperQuery.params.sortOrder = ''
      }
    },

    getScoreClass(row) {
      if (row.state !== 2) return ''
      if (row.userScore >= row.qualifyScore) return 'score-pass'
      return 'score-fail'
    },

    async openStats() {
      this.statsVisible = true
      this.statsData = null
      try {
        const res = await paperStats(this.examId)
        if (res.code === 0) {
          this.statsData = res.data
        }
      } catch (e) {
        this.$message.error('获取统计数据失败')
      }
    },

    getBarColor(label) {
      const colors = {
        '0-59分': '#ff4d4f',
        '60-69分': '#faad14',
        '70-79分': '#1890ff',
        '80-89分': '#52c41a',
        '90-100分': '#13c2c2'
      }
      return colors[label] || '#1890ff'
    }
  }
}
</script>

<style scoped>
.score-pass {
  color: #52c41a;
  font-weight: bold;
}
.score-fail {
  color: #ff4d4f;
  font-weight: bold;
}

.stats-container {
  padding: 0;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.summary-item {
  text-align: center;
  padding: 12px 8px;
  background: #f5f7fa;
  border-radius: 8px;
}

.summary-item.pass {
  background: #f6ffed;
}

.summary-item.fail {
  background: #fff2f0;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.summary-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.bar-chart {
  padding: 0 20px;
}

.bar-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.bar-label {
  width: 70px;
  font-size: 12px;
  color: #666;
  text-align: right;
  padding-right: 10px;
}

.bar-track {
  flex: 1;
  height: 28px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 36px;
  transition: width 0.5s ease;
}

.bar-count {
  color: #fff;
  font-size: 11px;
  padding-right: 6px;
  white-space: nowrap;
}

.pass-pie {
  margin-top: 24px;
  text-align: center;
}

.pie-visual {
  display: flex;
  justify-content: center;
}

.pie-ring {
  position: relative;
  display: inline-block;
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.pie-value {
  font-size: 28px;
  font-weight: 700;
  color: #52c41a;
}

.pie-label {
  font-size: 12px;
  color: #999;
}
</style>
