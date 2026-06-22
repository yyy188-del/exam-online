import { post } from '@/utils/request'

export function fetchPaging(data) {
  return post('/exam/api/notice/paging', data)
}

export function fetchDetail(id) {
  return post('/exam/api/notice/detail', { id: id })
}

export function saveData(data) {
  return post('/exam/api/notice/save', data)
}

export function deleteData(ids) {
  return post('/exam/api/notice/delete', { ids: ids })
}

export function changeState(ids, state) {
  return post('/exam/api/notice/state', { ids: ids, state: state })
}
