import request from '@/utils/request'

/**
 * 获取课程报告列表
 */
export function getCourseReports(courseId) {
  return request({
    url: `/reports/course/${courseId}`,
    method: 'get'
  })
}

/**
 * 创建报告作业
 */
export function createReport(data) {
  return request({
    url: '/reports',
    method: 'post',
    data
  })
}

/**
 * 更新报告作业
 */
export function updateReport(id, data) {
  return request({
    url: `/reports/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除报告作业
 */
export function deleteReport(id) {
  return request({
    url: `/reports/${id}`,
    method: 'delete'
  })
}

/**
 * 获取报告详情
 */
export function getReportDetail(id) {
  return request({
    url: `/reports/${id}`,
    method: 'get'
  })
}

/**
 * AI批改报告
 */
export function reviewReportWithAI(data) {
  return request({
    url: '/reports/review-ai',
    method: 'post',
    data
  })
}

/**
 * 获取报告提交记录
 */
export function getReportSubmissions(reportId) {
  return request({
    url: `/reports/${reportId}/submissions`,
    method: 'get'
  })
}

/**
 * 提交报告
 */
export function submitReport(reportId, data) {
  return request({
    url: `/reports/${reportId}/submit`,
    method: 'post',
    data
  })
}
