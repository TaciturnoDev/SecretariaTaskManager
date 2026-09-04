let sectors = [];

/* ================= CARREGAR SETORES ================= */

async function carregarSetores() {

    try {

        const response = await fetch("/sectors/with-users");

        if (!response.ok) {
            throw new Error("Erro ao buscar setores");
        }

        sectors = await response.json();

        renderSetores();

    } catch (e) {

        console.error("Erro ao carregar setores:", e);

    }

}

/* ================= RENDER ================= */

function renderSetores() {

    const grid = document.getElementById("sectorGrid");

    if (!grid) return;

    grid.innerHTML = "";

    if (!sectors || sectors.length === 0) {

        grid.innerHTML = `
            <p style="color:#64748b;">
                Nenhum setor encontrado.
            </p>
        `;

        return;
    }

    sectors.forEach(sector => {

        const users = sector.users || [];

        const card = document.createElement("div");

        card.className = "sector-card";

        card.innerHTML = `

            <div class="sector-card-top">

                <div class="sector-card-title">

                    <div class="sector-avatar blue">
                        🏢
                    </div>

                    <div>
                        <h3>${sector.name}</h3>

                        <span class="sector-status active">
                            Ativo
                        </span>
                    </div>

                </div>

            </div>

            <div class="sector-card-body">

                <div class="sector-line">
                    <span>Usuários</span>
                    <strong>${users.length}</strong>
                </div>

                <div class="sector-line">
                    <span>Equipe</span>
                    <strong>
					${users.length} membros
					</strong>
                </div>

            </div>

			<button class="manage-btn">
			    Gerenciar
			</button>

			<div class="sector-manage-panel">

			    <h4 class="manage-title">
			        Usuários do setor
			    </h4>

			    ${
			        users.length > 0

			        ? users.map(user => `

			            <div class="sector-user-row">

			                <div class="sector-user-left">

			                    <div class="user-status online"></div>

			                    <span class="sector-user-name">
			                        ${user.name}
			                    </span>

			                </div>

			                <div class="sector-user-actions">

			                    <button class="chat-btn">
			                        Conversar
			                    </button>

								<button
								    class="task-btn"
									onclick="requestTask(
									    ${user.id},
									    '${user.name.replace(/'/g, "\\'")}'
									)">
								    Solicitar Demanda
								</button>

			                </div>

			            </div>

			        `).join("")

			        : `

			            <div class="empty-users">
			                Nenhum usuário neste setor.
			            </div>

			        `
			    }

			</div>

        `;

        grid.appendChild(card);

		const manageBtn = card.querySelector(".manage-btn");

		const panel = card.querySelector(".sector-manage-panel");

		manageBtn.addEventListener("click", () => {

		    panel.classList.toggle("open");

		});
    });

}

/* ================= SOLICITAR DEMANDA ================= */

function requestTask(userId, userName) {

    sessionStorage.setItem(
        "pendingTaskRequest",
        JSON.stringify({
            userId: userId,
            userName: userName
        })
    );

    console.log("Solicitação salva.");

}
/* ================= INIT ================= */

document.addEventListener("DOMContentLoaded", () => {

    carregarSetores();

});