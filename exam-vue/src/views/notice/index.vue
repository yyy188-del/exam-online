<template>
  <div>
    <data-table
      ref="pagingTable"
      :options="options"
      :list-query="listQuery"
    >
      <template #filter-content>

        <el-input v-model="listQuery.params.title" placeholder="搜索公告标题" style="width: 200px;" class="filter-item" />

      </template>

      <template #data-columns>

        <el-table-column
          label="公告标题"
          prop="title"
        />

        <el-table-column
          label="创建时间"
          align="center"
          width="180px"
        >
          <template v-slot="scope">
            {{ scope.row.createTime | formatTime }}
          </template>
        </el-table-column>

        <el-table-column
          label="状态"
          align="center"
          width="100px"
        >
          <template v-slot="scope">
            <el-tag :type="scope.row.state === 0 ? 'success' : 'info'">
              {{ scope.row.state === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          align="center"
          width="220px"
        >
          <template v-slot="scope">
            <el-button type="primary" size="mini" icon="el-icon-edit" @click="handleEdit(scope.row.id)">修改</el-button>
            <el-button type="info" size="mini" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>

      </template>

    </data-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" @closed="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.state">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="公告详情" :visible.sync="viewVisible" width="500px">
      <div style="line-height: 2;">
        <h3 style="text-align: center; margin-bottom: 15px;">{{ viewData.title }}</h3>
        <div style="color: #666; text-align: center; margin-bottom: 15px;">
          发布时间：{{ viewData.createTime | formatTime }}
        </div>
        <div style="padding: 10px; background: #f9f9f9; border-radius: 5px; min-height: 100px;">
          {{ viewData.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import DataTable from '@/components/DataTable'
import { fetchDetail, saveData } from '@/api/notice'

export default {
  name: 'NoticeList',
  components: { DataTable },
  filters: {
    formatTime(val) {
      if (!val) return ''
      const d = new Date(val)
      return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0') + ' ' +
        String(d.getHours()).padStart(2, '0') + ':' +
        String(d.getMinutes()).padStart(2, '0')
    }
  },
  data() {
    return {
      listQuery: {
        current: 1,
        size: 10,
        params: {
          title: ''
        }
      },

      options: {
        multi: true,
        multiActions: [
          { value: 'delete', label: '删除' },
          { value: 'enable', label: '启用' },
          { value: 'disable', label: '禁用' }
        ],
        listUrl: '/exam/api/notice/paging',
        deleteUrl: '/exam/api/notice/delete',
        stateUrl: '/exam/api/notice/state',
        addRoute: ''
      },

      dialogTitle: '添加公告',
      dialogVisible: false,
      viewVisible: false,
      viewData: {},
      isEdit: false,
      form: {
        id: '',
        title: '',
        content: '',
        state: 0
      },
      rules: {
        title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleAdd() {
      this.dialogTitle = '添加公告'
      this.isEdit = false
      this.form = { id: '', title: '', content: '', state: 0 }
      this.dialogVisible = true
    },
    handleEdit(id) {
      this.dialogTitle = '修改公告'
      this.isEdit = true
      fetchDetail(id).then(res => {
        this.form = res.data
        this.dialogVisible = true
      })
    },
    handleView(row) {
      this.viewData = row
      this.viewVisible = true
    },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          saveData(this.form).then(res => {
            if (res.code === 0) {
              this.$message.success(this.isEdit ? '修改成功' : '添加成功')
              this.dialogVisible = false
              this.$refs.pagingTable.getList()
            }
          })
        }
      })
    },
    resetForm() {
      this.$refs.form && this.$refs.form.resetFields()
    }
  }
}
</script>
