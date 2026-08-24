const state = { token: sessionStorage.getItem('agentflow.token') || '' };
const $ = (selector) => document.querySelector(selector);

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const response = await fetch(path, { ...options, headers });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok || payload.success === false) {
    throw new Error(payload.message || `请求失败 (${response.status})`);
  }
  return payload.data;
}

function setAuthenticated(authenticated) {
  $('#login-panel').hidden = authenticated;
  $('#workspace').hidden = !authenticated;
  $('#logout-button').hidden = !authenticated;
}

function renderOverview(data) {
  $('#user-pill').textContent = data.username;
  $('#project-count').textContent = data.projectCount;
  $('#task-count').textContent = data.taskCount;
  $('#approval-count').textContent = data.pendingApprovalCount;
  $('#run-count').textContent = data.recentAgentRuns.length;
  $('#generated-at').textContent = new Date(data.generatedAt).toLocaleString();

  const total = Math.max(1, Object.values(data.tasksByStatus).reduce((sum, value) => sum + value, 0));
  $('#status-bars').innerHTML = Object.entries(data.tasksByStatus).map(([status, count]) => `
    <div class="status-row"><span>${status}</span><div class="bar"><i style="width:${Math.max(3, count / total * 100)}%"></i></div><strong>${count}</strong></div>
  `).join('');

  $('#run-list').innerHTML = data.recentAgentRuns.length ? data.recentAgentRuns.map(run => `
    <div class="run">
      <code>${escapeHtml(run.runId.slice(0, 8))}</code>
      <div><p>${escapeHtml(run.request)}</p><small>${new Date(run.startedAt).toLocaleString()} · ${run.modelCalls} model / ${run.toolCalls} tools</small></div>
      <span class="badge ${run.status === 'COMPLETED' ? '' : 'failed'}">${run.status}</span>
    </div>
  `).join('') : '<p class="empty">暂无 Agent 运行记录</p>';
}

function escapeHtml(value = '') {
  return value.replace(/[&<>'"]/g, character => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' })[character]);
}

async function refresh() {
  if (!state.token) return;
  try {
    const data = await api('/api/v1/dashboard/overview');
    renderOverview(data);
    setAuthenticated(true);
    $('.status-dot').style.background = '#53e6d1';
    $('#health-text').textContent = '服务运行正常';
  } catch (error) {
    if (/401|认证|token/i.test(error.message)) logout();
    $('#health-text').textContent = error.message;
  }
}

$('#login-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const message = $('#login-message');
  message.textContent = '正在验证身份…';
  try {
    const token = await api('/api/v1/auth/login', { method: 'POST', body: JSON.stringify(Object.fromEntries(form)) });
    state.token = token.accessToken;
    sessionStorage.setItem('agentflow.token', state.token);
    message.textContent = '';
    await refresh();
  } catch (error) { message.textContent = error.message; }
});

function logout() {
  state.token = '';
  sessionStorage.removeItem('agentflow.token');
  $('#user-pill').textContent = 'Guest';
  setAuthenticated(false);
}

$('#refresh-button').addEventListener('click', refresh);
$('#logout-button').addEventListener('click', logout);
document.querySelectorAll('.nav-item').forEach(button => button.addEventListener('click', () => {
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  $('#page-title').textContent = button.textContent.trim();
}));

setAuthenticated(Boolean(state.token));
refresh();
