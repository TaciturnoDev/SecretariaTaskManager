let loggedUser = null;
let loggedUserSectorId = null;

let tasks = [];
let filteredTasks = [];
let currentPage = 1;
let editingTaskId = null;
let attachmentToReplace = null;

const ITEMS_PER_PAGE = 6;

/* ================= NORMALIZAR CPF ================= */
function normalizeCPF(cpf) {
    if (!cpf) return "";
    return cpf.replace(/\D/g, "");
}

/* ================= LOGOUT ================= */
async function logout() {
    try {
        await fetch("/logout", { method: "POST" });
    } catch (e) {
        console.error("Erro ao sair:", e);
    } finally {
        window.location.href = "/login";
    }
}

/* ================= SIDEBAR ================= */
function toggleSidebar() {
    const sidebar = document.querySelector(".sidebar");

    if (sidebar) {
        sidebar.classList.toggle("open");
    }
}

/* ================= FILTRO SETORES ================= */
function filtrarSetores() {

    const input = document.getElementById("searchSetor");

    if (!input) return;

    const filter = input.value.toLowerCase();

    const items = document.querySelectorAll(".sector-item");

    items.forEach(item => {

        const text = item.textContent.toLowerCase();

        item.style.display = text.includes(filter)
            ? ""
            : "none";
    });
}

/* ================= CRIAR / ATUALIZAR ================= */
async function createTask() {

    if (!loggedUser) {
        alert("Usuário não autenticado");
        return;
    }

    const title = document.getElementById("title").value.trim();

    const description = document.getElementById("description").value.trim();
	
	if (description.length > 1000) {
	    alert("Descrição deve ter no máximo 1000 caracteres.");
	    return;
	}
	
    const status =
        document.getElementById("status")?.value || "PENDING";

    const priority =
        document.getElementById("priority")?.value || "MEDIUM";

    if (!title) return;

	let selectedSector = null;
	
    try {

        const url = editingTaskId
            ? `/tasks/${editingTaskId}`
            : "/tasks";

        const method = editingTaskId
            ? "PUT"
            : "POST";

        const bodyData = {
            title,
            description,
            status,
            priority
        };

        // ================= SUPERADMIN =================
		if (loggedUser.role === "SUPERADMIN") {

		    selectedSector =
		        document.getElementById("taskSectorSelect")?.value;

		    if (
		        !selectedSector ||
		        selectedSector === "" ||
		        selectedSector === "null"
		    ) {
		        alert("Selecione um setor");
		        return;
		    }

		    bodyData.sectorId =
		        parseInt(selectedSector);

		} else {

            if (loggedUserSectorId) {
                bodyData.sectorId = loggedUserSectorId;
            }
        }
        
		/* remover linha abaixo apos erro parar */
		console.log("Usuário logado:", loggedUser);

		console.log(
		    "Role usuário:",
		    loggedUser?.role
		);

		console.log(
		    "Setor selecionado:",
		    selectedSector
		);

		console.log(
		    "Body enviada:",
		    bodyData
		);
		/* até aqui */
		
        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(bodyData)
        });

        if (response.ok) {

            await carregarTarefas();

            limparFormulario();

            editingTaskId = null;

            document.getElementById("submitBtn").innerText = "Criar";

        } else {

            const errorText = await response.text();

            console.error("Erro backend:", errorText);

            alert("Erro ao salvar tarefa");
        }

    } catch (error) {

        console.error("Erro:", error);
    }
}

/* ================= LIMPAR ================= */
function limparFormulario() {

    document.getElementById("title").value = "";

    document.getElementById("description").value = "";

    const statusEl = document.getElementById("status");

    if (statusEl) {
        statusEl.value = "PENDING";
    }

    const priorityEl = document.getElementById("priority");

    if (priorityEl) {
        priorityEl.value = "MEDIUM";
    }
}

/* ================= FILTROS ================= */
function aplicarFiltros() {

    const searchRaw =
        document.getElementById("searchInput")
            ?.value
            .toLowerCase()
            .trim() || "";

    const searchCPF = normalizeCPF(searchRaw);

    const statusFilter =
        document.getElementById("filterStatus")?.value || "ALL";

    const userFilter =
        document.getElementById("filterUser")?.value || "ALL";

    filteredTasks = tasks.filter(task => {

        const matchUser =
            userFilter === "ALL" ||
            (
                task.assignedToName &&
                task.assignedToName === userFilter
            );

        const matchStatus =
            statusFilter === "ALL" ||
            task.status === statusFilter;

        const title =
            task.title?.toLowerCase() || "";

        const description =
            task.description?.toLowerCase() || "";

        const taskCPF =
            normalizeCPF(task.cpf || "");

        const matchSearch =
            !searchRaw ||
            title.includes(searchRaw) ||
            description.includes(searchRaw) ||
            taskCPF.includes(searchCPF);

        return matchUser && matchStatus && matchSearch;
    });

    currentPage = 1;

    render();
}

/* ================= DASHBOARD ================= */
function updateDashboard() {

    document.getElementById("totalTasks").innerText =
        tasks.length;

    document.getElementById("pendingTasks").innerText =
        tasks.filter(t => t.status === "PENDING").length;

    document.getElementById("progressTasks").innerText =
        tasks.filter(t => t.status === "IN_PROGRESS").length;

    document.getElementById("doneTasks").innerText =
        tasks.filter(t => t.status === "COMPLETED").length;
}


/* ================= RENDER ================= */
function render() {

    renderTasks();

    renderPagination();
}

/* ================= TAREFAS ================= */
function renderTasks() {

    const list = document.getElementById("taskList");

    if (!list) return;

    list.innerHTML = "";

    if (!filteredTasks || filteredTasks.length === 0) {

        list.innerHTML =
            "<p style='text-align:center;'>Nenhuma tarefa encontrada.</p>";

        return;
    }

    const start = (currentPage - 1) * ITEMS_PER_PAGE;

    const end = start + ITEMS_PER_PAGE;

    const pageItems = filteredTasks.slice(start, end);

    pageItems.forEach(task => {

		const li = document.createElement("li");

		li.id = `task-${task.id}`;

		li.className = `task-card ${getPriorityClass(task.priority)}`;

        li.innerHTML = `

            <div class="task-info">

                <strong>${task.title}</strong>

				<p class="task-description">
				    ${task.description || ""}
				</p>

                <div class="task-meta">

                    <div class="task-user-badge">
                        👤 Responsável:
                        ${task.assignedToName?.toUpperCase() || "—"}
                    </div>

                    <div class="task-created-by">
                        ✍️ Criado por:
                        ${task.createdByName || "—"}
                    </div>

                    <div class="task-priority">
                        🚨 Prioridade:
                        ${getPriorityLabel(task.priority)}
                    </div>

                    <div class="task-date">
                        📅
                        ${task.createdAt
                            ? formatDate(task.createdAt)
                            : ""}
                    </div>

                </div>
				
				
            </div>

            <div class="status-badge ${getStatusClass(task.status)}">
                ${getStatusLabel(task.status)}
            </div>

	<div class="task-actions">

			    <button
			        title="Abrir tarefa"
			        onclick="openTaskModal(${task.id})"
			    >
			        👁️
			    </button>

			    <button
			        title="Editar tarefa"
			        onclick="editTask(${task.id})"
			    >
			        ✏️
			    </button>

			    <button
			        title="Delegar tarefa"
			        onclick="openDelegateModal(${task.id})"
			    >
			        🔄
			    </button>

			    <button
			        title="Excluir tarefa"
			        onclick="deleteTask(${task.id})"
			    >
			        ❌
			    </button>

			</div>

			
        `;

        list.appendChild(li);
    });
}

/* ================= DELETE ================= */
async function deleteTask(id) {

    if (!confirm("Deseja excluir esta tarefa?")) return;

    try {

        const response = await fetch(`/tasks/${id}`, {
            method: "DELETE"
        });

        if (response.ok) {

            await carregarTarefas();

        } else {

            alert("Erro ao excluir");
        }

    } catch (e) {

        console.error("Erro ao deletar:", e);
    }
}

/* ================= EDITAR ================= */
function editTask(id) {

    const task = tasks.find(t => t.id === id);

    if (!task) return;

    document.getElementById("title").value =
        task.title;

    document.getElementById("description").value =
        task.description || "";

    document.getElementById("status").value =
        task.status;

    document.getElementById("priority").value =
        task.priority;

    editingTaskId = id;

    document.getElementById("submitBtn").innerText =
        "Atualizar";
}

/* ================= PAGINAÇÃO ================= */
function renderPagination() {

    const totalPages =
        Math.ceil(filteredTasks.length / ITEMS_PER_PAGE);

    if (currentPage > totalPages) {
        currentPage = totalPages || 1;
    }

    const containers = [
        document.getElementById("paginationTop"),
        document.getElementById("paginationBottom")
    ];

    containers.forEach(container => {

        if (!container) return;

        container.innerHTML = "";

        if (totalPages <= 1) return;

        for (let i = 1; i <= totalPages; i++) {

            const btn = document.createElement("button");

            btn.textContent = i;

            if (i === currentPage) {
                btn.classList.add("active");
            }

            btn.onclick = () => {

                currentPage = i;

                render();
            };

            container.appendChild(btn);
        }
    });
}

/* ================= STATUS ================= */
function getStatusClass(status) {

    if (status === "PENDING") {
        return "status-pending";
    }

    if (status === "IN_PROGRESS") {
        return "status-in-progress";
    }

    return "status-completed";
}

function getStatusLabel(status) {

    if (status === "PENDING") {
        return "Pendente";
    }

    if (status === "IN_PROGRESS") {
        return "Em andamento";
    }

    return "Concluída";
}

/* ================= PRIORIDADE ================= */
function getPriorityLabel(priority) {

    if (priority === "LOW") {
        return "Pequena";
    }

    if (priority === "MEDIUM") {
        return "Média";
    }

    if (priority === "HIGH") {
        return "Alta";
    }

    if (priority === "URGENT") {
        return "Urgente";
    }

    return "Não definida";
}

/* ================= PRIORITY CLASS ================= */
function getPriorityClass(priority) {

    if (priority === "HIGH") {
        return "priority-high";
    }

    if (priority === "URGENT") {
        return "priority-urgent";
    }

    return "priority-normal";
}

/* ================= DATA ================= */
function formatDate(dateString) {

    const date = new Date(dateString);

    return isNaN(date)
        ? ""
        : date.toLocaleDateString("pt-BR");
}

/* ================= USER FILTER ================= */
function updateUserFilterOptions() {

    const select =
        document.getElementById("filterUser");

    if (!select) return;

    const users = [
        ...new Set(tasks.map(t => t.assignedToName))
    ];

    select.innerHTML =
        `<option value="ALL">Todos</option>`;

    users.forEach(user => {

        if (!user) return;

        const option = document.createElement("option");

        option.value = user;

        option.textContent = user;

        select.appendChild(option);
    });
}


/* ================= CARREGAR SELECT DE SETORES ================= */

async function loadSectorFilter() {

    try {

        const response =
            await fetch("/sectors");

        if (!response.ok) {
            throw new Error("Erro ao buscar setores");
        }

        const sectors = await response.json();

        const select =
            document.getElementById("taskSectorSelect");

        if (!select) return;

        select.innerHTML =
            `<option value="">Selecione o setor</option>`;

        sectors.forEach(sector => {

            const option =
                document.createElement("option");

            option.value = sector.id;

            option.textContent = sector.name;

            select.appendChild(option);

        });

    } catch (e) {

        console.error(
            "Erro ao carregar setores:",
            e
        );
    }
}


/* ================= USUÁRIO LOGADO ================= */
async function carregarUsuarioLogado() {

    try {

        const response = await fetch("/auth/me");

        if (!response.ok) {
            throw new Error("Não autenticado");
        }

        const data = await response.json();

        loggedUser = data;

        loggedUserSectorId = data.sectorId || null;

        document.getElementById("loggedUserName").innerText =
            data.username;

        // ================= SUPERADMIN =================
        if (loggedUser.role === "SUPERADMIN") {

            const sectorSelect =
                document.getElementById("taskSectorSelect");

            if (sectorSelect) {

                sectorSelect.style.display =
                    "inline-block";

                await loadSectorFilter();
            }
        }

    } catch (e) {

        console.error("Erro ao pegar usuário:", e);
    }
}

/* ================= CARREGAR TAREFAS ================= */
async function carregarTarefas() {

    try {

        const response =
            await fetch("/tasks?page=0&size=50");

        if (!response.ok) {
            throw new Error("Erro ao buscar tarefas");
        }

        const data = await response.json();

        tasks = Array.isArray(data)
            ? data
            : (data.content || []);

        tasks.sort(
            (a, b) =>
                new Date(b.createdAt) -
                new Date(a.createdAt)
        );

		filteredTasks = [...tasks];

		updateDashboard();

		updateUserFilterOptions();

		aplicarFiltros();

    } catch (e) {

        console.error(e);

        tasks = [];

        filteredTasks = [];

        render();
    }
}


/* ================= ATUALIZAR UMA TAREFA ================= */

async function refreshTask(taskId) {

    try {

        const response =
            await fetch(`/tasks/${taskId}`);

        if (!response.ok) {
            throw new Error(
                "Erro ao atualizar tarefa."
            );
        }

        const updatedTask =
            await response.json();

        const index =
            tasks.findIndex(
                t => t.id === taskId
            );

        if (index >= 0) {

            tasks[index] =
                updatedTask;

        } else {

            tasks.push(
                updatedTask
            );
        }

        aplicarFiltros();

        return updatedTask;

    } catch (e) {

        console.error(e);

        return null;
    }
}

/* ================= INIT ================= */
document.addEventListener("DOMContentLoaded", () => {

    carregarUsuarioLogado();

    // ================= TELA DE TAREFAS =================
    if (document.getElementById("taskList")) {

        carregarTarefas();

        document.getElementById("filterUser")
            ?.addEventListener("change", aplicarFiltros);
    }
});


/* ================= RENDER MODAL ================= */

function renderTaskModal(task) {

    const overlay =
        document.getElementById("taskModalOverlay");

    const content =
        document.getElementById("taskModalContent");

		let historyHtml =
		    "<p>Nenhum histórico.</p>";

		if (task.history && task.history.length > 0) {

		    const firstHistory =
		        task.history[task.history.length - 1];

		    const lastHistory =
		        task.history[0];

		    const resumeHistory = [];

		    if (firstHistory) {
		        resumeHistory.push(firstHistory);
		    }

		    if (
		        lastHistory &&
		        lastHistory.id !== firstHistory.id
		    ) {
		        resumeHistory.push(lastHistory);
		    }

		    historyHtml = `

		        <div class="history-resume">

		            ${resumeHistory.map((item, index) => `

		                <div class="history-item">

		                    <div class="history-header">

		                        <div class="history-user">
		                            👤 ${item.userName}
		                        </div>

		                        <div class="history-date">
		                            ${formatDate(item.createdAt)}
		                        </div>

		                    </div>

		                    <div class="history-action">
		                        ${item.action}
		                    </div>

		                </div>

		            `).join("")}

		        </div>

		        <button
		            class="history-expand-btn"
		            onclick="toggleFullHistory()"
		        >
		            Ver histórico completo
		        </button>

		        <div
		            id="fullHistoryContainer"
		            style="display:none;"
		        >

		            ${task.history.map((item, index) => `
						
            <div class="history-item">

                <div class="history-header">

                    <div class="history-user">
                        👤 ${item.userName}
                    </div>

                    <div class="history-date">
                        ${formatDate(item.createdAt)}
                    </div>

                </div>

                <div class="history-action">
                    ${item.action}
                </div>

                ${
                    item.attachments &&
                    item.attachments.length > 0
                    ? `

                    <div class="history-attachments">

                        <h4>Anexos</h4>

						${item.attachments.map(att => `

						    <div class="attachment-item">

						        <a
						            href="/attachments/download/${att.id}"
						            target="_blank"
						        >
						            📎 ${att.originalFileName}
						        </a>

						        <span>
						            (${(att.fileSize / 1024).toFixed(1)} KB)
						        </span>

						        <button
						            class="attachment-update-btn"
						            onclick="selectAttachmentUpdate(${att.id})"
						        >
						            Atualizar arquivo
						        </button>

						    </div>

						`).join("")}

                    </div>

                    `
                    : ""
                }

                ${
                    (
                        item.oldTitle ||
                        item.oldDescription
                    ) &&
                    (
                        item.newTitle ||
                        item.newDescription
                    )
                    ? `


					<div class="history-change">

					    ${
					        item.oldTitle
					        ? `
					        <p>
					            <strong>Título anterior:</strong>
					            ${item.oldTitle}
					        </p>
					        `
					        : ""
					    }

					    ${
					        item.newTitle
					        ? `
					        <p>
					            <strong>Novo título:</strong>
					            ${item.newTitle}
					        </p>
					        `
					        : ""
					    }

					    ${
					        item.oldDescription
					        ? `
					        <p>
					            <strong>Descrição anterior:</strong>
					            ${item.oldDescription}
					        </p>
					        `
					        : ""
					    }

					    ${
					        item.newDescription
					        ? `
					        <p>
					            <strong>Nova descrição:</strong>
					            ${item.newDescription}
					        </p>
					        `
					        : ""
					    }

					</div>

                    `
                    : ""
                }

				            </div>

				        `).join("")}

				        </div>

				    `;
				}

    content.innerHTML = `

	<div class="task-modal-header">

	    <h2>
	        ${task.title}
	    </h2>

	    <button
	        class="report-btn"
	        onclick="downloadTaskReport(${task.id})"
	        title="Gerar relatório"
	    >
	        📄 Relatório
	    </button>

	</div>

	<hr>

		<div class="task-info-grid">

		    <div class="task-info-card">

		        <span>Status</span>

		        <strong>
		            ${getStatusLabel(task.status)}
		        </strong>

		    </div>

		    <div class="task-info-card">

		        <span>Prioridade</span>

		        <strong>
		            ${getPriorityLabel(task.priority)}
		        </strong>

		    </div>

		    <div class="task-info-card">

		        <span>Criado por</span>

		        <strong>
		            ${task.createdByName || "-"}
		        </strong>

		    </div>

		    <div class="task-info-card">

		        <span>Responsável</span>

		        <strong>
		            ${task.assignedToName || "-"}
		        </strong>
				

		    </div>

		</div>

        <hr>

		<div class="modal-section">

		    <h3>Descrição</h3>

		    <div class="task-description-card">

		        ${task.description || "Sem descrição"}

		    </div>

		</div>

        <hr>

		<div class="modal-section">

		    <h3>Histórico</h3>

		    <div class="task-history">

		        ${historyHtml}

		    </div>

		</div>

        <hr>

		<div class="modal-section">

		    <h3>Anexar arquivo</h3>

		    <div class="upload-area">

		        <input
		            type="file"
		            id="taskAttachmentFile"
		        >
				
				<input
				    type="file"
				    id="replaceAttachmentFile"
				    style="display:none"
				>

		        <div class="upload-placeholder">

		            <div class="upload-icon">
		                📎
		            </div>

		            <div class="upload-text">

		                Selecione um arquivo

		                <small>
		                    Documentos, imagens, áudio ou vídeo
		                </small>

		            </div>

		        </div>

		    </div>

		    <button
		        class="upload-btn"
		        onclick="uploadAttachment(${task.id})"
		    >
		        Enviar arquivo
		    </button>

		</div>

    `;

    overlay.style.display = "flex";
}

/* ================= MODAL TAREFA ================= */

async function openTaskModal(taskId) {

    const task =
        tasks.find(t => t.id === taskId);

    if (!task) return;

    renderTaskModal(task);
}

/* ================= FECHAR MODAL ================= */

function closeTaskModal() {

    const overlay =
        document.getElementById("taskModalOverlay");

    overlay.style.display = "none";
}


/* =============== download pdf ================== */

function downloadTaskReport(taskId) {

    if (!taskId) {
        console.error("ID da tarefa não informado.");
        return;
    }

    window.open(`/tasks/${taskId}/report`, "_blank");

}

/* ================= ATUALIZAR ANEXO ================= */

function selectAttachmentUpdate(attachmentId) {

    attachmentToReplace =
        attachmentId;

    const input =
        document.getElementById(
            "replaceAttachmentFile"
        );

    if (!input) {

        alert(
            "Campo de atualização não encontrado."
        );

        return;
    }

    input.onchange = async function () {

        if (this.files.length === 0) {
            return;
        }

        const formData =
            new FormData();

        formData.append(
            "file",
            this.files[0]
        );

        try {

            const response =
                await fetch(
                    `/attachments/replace/${attachmentToReplace}`,
                    {
                        method: "POST",
                        body: formData
                    }
                );

            if (response.ok) {

                alert(
                    "Arquivo atualizado com sucesso."
                );

                const updatedTask =
                    await refreshTask(
                        tasks.find(t =>
                            t.history?.some(h =>
                                h.attachments?.some(a =>
                                    a.id === attachmentToReplace
                                )
                            )
                        )?.id
                    );

                if (updatedTask) {

                    renderTaskModal(
                        updatedTask
                    );
                }

            } else {

                alert(
                    "Erro ao atualizar arquivo."
                );
            }

        } catch (e) {

            console.error(e);

            alert(
                "Erro ao atualizar arquivo."
            );
        }

        this.value = "";

        attachmentToReplace =
            null;
    };

    input.click();
}

/* ================= TOGGLE HISTÓRICO ================= */

function toggleHistoryDetails(index) {

    const el =
        document.getElementById(
            `history-details-${index}`
        );

    if (!el) return;

    el.classList.toggle("open");
}


/* =============== HISTORICO EXPANDIDO ================ */


function toggleFullHistory() {

    const container =
        document.getElementById(
            "fullHistoryContainer"
        );

    if (!container) return;

    if (
        container.style.display === "none"
    ) {

        container.style.display = "block";

    } else {

        container.style.display = "none";
    }
}
 

/* ================= EVENTOS MODAL ================= */

document.addEventListener("DOMContentLoaded", () => {

    const overlay =
        document.getElementById("taskModalOverlay");

    const closeBtn =
        document.getElementById("closeTaskModalBtn");

    /* FECHAR NO X */
    closeBtn?.addEventListener(
        "click",
        closeTaskModal
    );

    /* FECHAR CLICANDO FORA */
    overlay?.addEventListener(
        "click",
        (e) => {

            if (
                e.target.id ===
                "taskModalOverlay"
            ) {
                closeTaskModal();
            }
        }
    );

});
		


/* ================= DELEGAR TAREFA ================= */

let selectedTaskToDelegate = null;

function openDelegateModal(taskId) {

    console.log("Abrindo modal de transferência:", taskId);

    const task = tasks.find(t => t.id === taskId);

    if (!task) return;

    selectedTaskToDelegate = taskId;
	
    loadDelegateUsers();

    document.getElementById("delegateTaskTitle").textContent =
        task.title || "-";

    document.getElementById("delegateCurrentResponsible").textContent =
        task.assignedToName || "-";

    const modal =
        document.getElementById("delegateModalOverlay");

    if (!modal) return;

    modal.style.display = "flex";
}

/* ================= CARREGAR USUÁRIOS PARA DELEGAÇÃO ================= */

async function loadDelegateUsers() {

    try {

        const response =
            await fetch("/tasks/delegation-users");

        if (!response.ok) {
            throw new Error("Erro ao carregar usuários.");
        }

        const data =
            await response.json();

        const select =
            document.getElementById("delegateUserSelect");

        select.innerHTML =
            `<option value="">Selecione um usuário</option>`;

        data.forEach(user => {

            select.innerHTML += `
                <option value="${user.id}">
                    ${user.name} - ${user.sectorName || "Sem setor"}
                </option>
            `;

        });

    } catch (e) {

        console.error("Erro ao carregar usuários:", e);
    }
}

/* ================= CONFIRMAR DELEGAÇÃO ================= */

async function confirmDelegate() {

    if (!selectedTaskToDelegate) {
        console.error("Nenhuma tarefa selecionada para delegação.");
        return;
    }

    const newResponsibleId =
        document.getElementById("delegateUserSelect").value;

    const comment =
        document.getElementById("delegateComment").value;

    if (!newResponsibleId) {
        alert("Selecione o novo responsável.");
        return;
    }

    const data = {
        targetUserId: Number(newResponsibleId),
        comment: comment
    };

    console.log("Delegação:", {
        taskId: selectedTaskToDelegate,
        data
    });


    try {

        const response = await fetch(
            `/tasks/${selectedTaskToDelegate}/forward`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            }
        );


        if (!response.ok) {

            const error = await response.text();

            console.error("Erro ao delegar tarefa:", error);

            alert("Erro ao delegar tarefa.");

            return;
        }


        console.log("Tarefa delegada com sucesso.");

        closeDelegateModal();

        await carregarTarefas();


    } catch (error) {

        console.error("Erro na delegação:", error);

        alert("Erro de comunicação com o servidor.");
    }
}

/* ================= FECHAR DELEGAÇÃO ================= */

function closeDelegateModal() {
    const modal = document.getElementById("delegateModalOverlay");

    if (modal) {
        modal.style.display = "none";
    }
}


	/* ================= UPLOAD DE ANEXO ================= */

	/*async function uploadAttachment(taskId) {*/
		window.uploadAttachment = async function (taskId) {

	    const input =
	        document.getElementById(
	            "taskAttachmentFile"
	        );

	    if (!input || input.files.length === 0) {

	        alert("Selecione um arquivo.");

	        return;
	    }

	    const formData =
	        new FormData();

	    formData.append(
	        "file",  
	        input.files[0]
	    );

	    try {

	        const response =
	            await fetch(

	                `/attachments/upload/${taskId}`,

	                {
	                    method: "POST",

	                    body: formData
	                }
	            );

	        if (response.ok) {

	            alert(
	                "Arquivo enviado com sucesso."
	            );

				const updatedTask =
				    await refreshTask(taskId);

				if (updatedTask) {

				    renderTaskModal(updatedTask);

				}

	        } else {

	            alert(
	                "Erro ao enviar arquivo."
	            );
	        }

	    } catch (e) {

	        console.error(e);

	        alert(
	            "Erro ao enviar arquivo."
	        );
	    }
     }

