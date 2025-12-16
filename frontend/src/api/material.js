import request from '@/utils/request'

/**
 * 教材制作API（模块二）
 */

// 创建教材
export function createMaterial(data) {
  return request({
    url: '/materials',
    method: 'post',
    data
  })
}

// 文本转PPT
export function textToPPT(data) {
  return request({
    url: '/materials/text-to-ppt',
    method: 'post',
    data
  })
}

// PPT转视频
export function pptToVideo(data) {
  return request({
    url: '/materials/ppt-to-video',
    method: 'post',
    data
  })
}

// 获取教材详情
export function getMaterialDetail(id) {
  return request({
    url: `/materials/${id}`,
    method: 'get'
  })
}

// 获取课程的教材列表
export function getMaterialsByCourse(courseId) {
  return request({
    url: `/materials/course/${courseId}`,
    method: 'get'
  })
}

// 获取章节的教材列表
export function getMaterialsByChapter(chapterId) {
  return request({
    url: `/materials/chapter/${chapterId}`,
    method: 'get'
  })
}

// 更新教材
export function updateMaterial(id, data) {
  return request({
    url: `/materials/${id}`,
    method: 'put',
    data
  })
}

// 删除教材
export function deleteMaterial(id) {
  return request({
    url: `/materials/${id}`,
    method: 'delete'
  })
}


