import request from '@/utils/request'

/**
 * 获取课程考试列表
 */
export function getCourseExams(courseId) {
  return request({
    url: `/exams/course/${courseId}`,
    method: 'get'
  })
}

/**
 * 创建考试
 */
export function createExam(data) {
  return request({
    url: '/exams',
    method: 'post',
    data
  })
}

/**
 * 更新考试
 */
export function updateExam(id, data) {
  return request({
    url: `/exams/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除考试
 */
export function deleteExam(id) {
  return request({
    url: `/exams/${id}`,
    method: 'delete'
  })
}

/**
 * 获取考试详情
 */
export function getExamDetail(id) {
  return request({
    url: `/exams/${id}`,
    method: 'get'
  })
}

/**
 * 添加试题
 */
export function addQuestion(data) {
  return request({
    url: '/exams/questions',
    method: 'post',
    data
  })
}

/**
 * 获取考试试题
 */
export function getExamQuestions(examId) {
  return request({
    url: `/exams/${examId}/questions`,
    method: 'get'
  })
}

/**
 * 获取考试提交记录
 */
export function getExamSubmissions(examId) {
  return request({
    url: `/exams/${examId}/submissions`,
    method: 'get'
  })
}

/**
 * 提交考试
 */
export function submitExam(examId, data) {
  return request({
    url: `/exams/${examId}/submit`,
    method: 'post',
    data
  })
}

/**
 * 检查学生是否已提交考试
 */
export function checkExamSubmission(examId, studentId) {
  return request({
    url: `/exams/${examId}/check-submission/${studentId}`,
    method: 'get'
  })
}
