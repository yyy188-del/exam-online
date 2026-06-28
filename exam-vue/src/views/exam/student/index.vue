<template>

  <div>

    <data-table
      ref="pagingTable"
      :options="options"
      :list-query="listQuery"
    >

      <template #filter-content>

        <el-input v-model="listQuery.params.userName" style="width: 200px" placeholder="搜索登录名" class="filter-item" />
        <el-input v-model="listQuery.params.realName" style="width: 200px" placeholder="搜索姓名" class="filter-item" />

        <el-button class="filter-item" type="primary" icon="el-icon-plus" @click="handleAdd">
          添加学生
        </el-button>

      </template>

      <template #data-columns>

        <el-table-column
          type="selection"
          width="55"
        />

        <el-table-column
          align="center"
          label="用户名"
        >
          <template v-slot="scope">
            <a @click="handleUpdate(scope.row)">{{ scope.row.userName }}</a>
          </template>

        </el-table-column>

        <el-table-column
          align="center"
          label="姓名"
          prop="realName"
        />

        <el-table-column
          align="center"
          label="创建时间"
          prop="createTime"
        />

        <el-table-column
          align="center"
          label="状态"
        >

          <template v-slot="scope">
            {{ scope.row.state | stateFilter }}
          </template>
        </el-table-column>

      </template>
    </data-table>

    <el-dialog :visible.sync="dialogVisible" title="添加学生" width="500px">

      <el-form :model="formData" label-position="left" label-width="100px">

        <el-form-item label="用户名">
          <el-input v-model="formData.userName" />
        </el-form-item>

        <el-form-item label="姓名">
          <el-input v-model="formData.realName" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="formData.password" placeholder="不修改请留空" type="password" />
        </el-form-item>

      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">确 定</el-button>
      </div>

    </el-dialog>

  </div>

</template>

<script>
import DataTable from '@/components/DataTable'
import { saveData } from '@/api/sys/user/user'

export default {
  name: 'StudentList',
  components: { DataTable },
  filters: {
    stateFilter(value) {
      const map = {
        '0': '正常',
        '1': '禁用'
      }
      return map[value]
    }
  },
  data() {
    return {

      dialogVisible: false,

      listQuery: {
        current: 1,
        size: 10,
        params: {
        }
      },

      formData: {
        avatar: ''
      },

      options: {
        listUrl: '/exam/api/sys/user/paging',
        stateUrl: '/exam/api/sys/user/state',
        deleteUrl: '/exam/api/sys/user/delete',
        multiActions: [
          {
            value: 'enable',
            label: '启用'
          }, {
            value: 'disable',
            label: '禁用'
          },
          {
            value: 'delete',
            label: '删除'
          }
        ]
      }
    }
  },

  created() {
  },

  methods: {

    handleAdd() {
      this.formData = {}
      this.dialogVisible = true
    },

    handleUpdate(row) {
      this.dialogVisible = true
      this.formData = row
      this.formData.roles = ['student']
      this.formData.password = null
    },

    handleSave() {
      this.formData.roles = ['student']
      saveData(this.formData).then(() => {
        this.$message({
          type: 'success',
          message: '学生修改成功!'
        })
        this.dialogVisible = false
        this.$refs.pagingTable.getList()
      })
    }
  }
}
</script>
