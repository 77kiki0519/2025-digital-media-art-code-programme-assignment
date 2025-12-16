import request from '@/utils/request'

/**
 * 章节练习API（模块三）
 */

// ========== 练习管理 ==========

// 创建章节练习
export function createExercise(data) {
  return request({
    url: '/chapter-exercises',
    method: 'post',
    data
  })
}

// 更新章节练习
export function updateExercise(id, data) {
  return request({
    url: `/chapter-exercises/${id}`,
    method: 'put',
    data
  })
}

// 删除章节练习
export function deleteExercise(id) {
  return request({
    url: `/chapter-exercises/${id}`,
    method: 'delete'
  })
}

// 获取练习详情
export function getExerciseDetail(id) {
  return request({
    url: `/chapter-exercises/${id}`,
    method: 'get'
  })
}

// 获取章节的所有练习
export function getExercisesByChapter(chapterId) {
  return request({
    url: `/chapter-exercises/chapter/${chapterId}`,
    method: 'get'
  })
}

// ========== 题目管理 ==========

// 添加练习题目
export function addExerciseQuestion(data) {
  return request({
    url: '/chapter-exercises/questions',
    method: 'post',
    data
  })
}

// 更新练习题目
export function updateExerciseQuestion(id, data) {
  return request({
    url: `/chapter-exercises/questions/${id}`,
    method: 'put',
    data
  })
}

// 删除练习题目
export function deleteExerciseQuestion(id) {
  return request({
    url: `/chapter-exercises/questions/${id}`,
    method: 'delete'
  })
}

// 获取练习的所有题目
export function getExerciseQuestions(exerciseId) {
  return request({
    url: `/chapter-exercises/${exerciseId}/questions`,
    method: 'get'
  })
}

// ========== 提交与批改 ==========

// 提交练习
export function submitExercise(data) {
  return request({
    url: '/chapter-exercises/submit',
    method: 'post',
    data
  })
}

// 获取学生的提交记录
export function getStudentSubmissions(studentId, exerciseId = null) {
  return request({
    url: `/chapter-exercises/submissions/student/${studentId}`,
    method: 'get',
    params: { exerciseId }
  })
}

// 获取练习的所有提交
export function getExerciseSubmissions(exerciseId) {
  return request({
    url: `/chapter-exercises/${exerciseId}/submissions`,
    method: 'get'
  })
}

// 获取提交详情
export function getSubmissionDetail(id) {
  return request({
    url: `/chapter-exercises/submissions/${id}`,
    method: 'get'
  })
}


