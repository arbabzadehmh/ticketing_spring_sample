let currentPage = 0;
const pageSize = 50;

/* =========================
   GLOBAL HELPERS (خیلی مهم)
========================= */

function formatDateTime(dtArray) {
    let [year, month, day, hour, minute] = dtArray;
    return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")} ${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
}

function applyDirectionToElement(el) {
    const p = el.querySelector("p");
    if (!p) return;

    const text = p.innerText.trim();
    const persianRegex = /[\u0600-\u06FF]/;

    if (persianRegex.test(text)) {
        el.classList.add("rtl");
        el.classList.remove("ltr");
    } else {
        el.classList.add("ltr");
        el.classList.remove("rtl");
    }
}

function createMessageElement(message) {

    const msgDiv = document.createElement("div");

    const extractedLabel =
        document.getElementById("ocrExtractedTextLabel")?.innerText || "OCR Text:";

    msgDiv.className =
        "chat-message message p-2 mb-2 rounded " +
        (message.senderRoleName === 'ROLE_ADMIN'
            ? 'msg-admin'
            : (message.senderRoleName === 'ROLE_MANAGER'
                ? 'msg-manager'
                : 'msg-customer'));

    let html = `
        <h6 class="text-muted d-block mt-1 ltr small-text">
            ${message.senderUsername}:
        </h6>
    `;

    if (message.content) {
        html += `<p class="mb-1">${message.content}</p>`;
    }

    if (
        message.attachments &&
        message.attachments.length > 0 &&
        message.attachments[0].extractedText
    ) {
        html += `
        <div class="alert alert-info mt-2 p-2 small">
            <strong>${extractedLabel}</strong>
            <span>${message.attachments[0].extractedText}</span>
        </div>`;
    }

    if (message.attachments && message.attachments.length > 0) {

        html += `<div class="attachments mt-2">`;

        message.attachments.forEach(att => {

            const downloadUrl = `/rest/images/${att.mongoFileId}/download`;
            const rawUrl = `/rest/images/${att.mongoFileId}/raw`;

            if (['JPG', 'PNG', 'BMP'].includes(att.fileType)) {

                html += `
                    <a href="${rawUrl}" target="_blank">
                        <img src="${rawUrl}" class="chat-thumb"/>
                    </a>`;
            }
            else if (att.fileType === 'PDF') {

                html += `
                    <a href="${downloadUrl}" target="_blank" class="d-block">
                        <i class="fas fa-file-pdf fa-2x text-danger"></i>
                        <small>${att.fileName}</small>
                    </a>`;
            }
            else {

                html += `
                    <a href="${downloadUrl}" target="_blank">
                        <i class="fas fa-file fa-2x"></i>
                        <small>${att.fileName}</small>
                    </a>`;
            }
        });

        html += `</div>`;
    }

    const formattedDate = Array.isArray(message.dateTime)
        ? formatDateTime(message.dateTime)
        : message.dateTime;

    html += `
        <small class="text-muted d-block mt-1 ltr small-text">
            ${formattedDate}
        </small>
    `;

    const isAdminSender =
        message.senderRoleName === 'ROLE_ADMIN' ||
        message.senderRoleName === 'ROLE_MANAGER';

    const seen = isAdminSender
        ? message.seenByCustomer
        : message.seenByAdmin;

    html += `
        <span class="msg-status ${seen ? 'seen' : 'unseen'}">
            ${seen ? '✔✔' : '✔'}
        </span>
    `;

    msgDiv.innerHTML = html;

    applyDirectionToElement(msgDiv);

    return msgDiv;
}

// ---------------------------------------------------------
function getCsrfToken() {
    return document.querySelector("meta[name='_csrf']").content;
}

function getCsrfHeader() {
    return document.querySelector("meta[name='_csrf_header']").content;
}

async function secureFetch(url, options = {}) {

    options.headers = {
        ...(options.headers || {}),
        [getCsrfHeader()]: getCsrfToken()
    };

    return fetch(url, options);
}

/* =========================
   MAIN
========================= */

document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('messageForm');
    const chatBox = document.getElementById('chat-box');
    const fileInput = document.getElementById('messageFile');
    const ocrBtn = document.getElementById('ocrSendBtn');
    const ticketCloseBtn = document.getElementById('ticketCloseBtn');
    const loadOlderBtn = document.getElementById('loadOlderBtn');

    const ocrProcessingTextElem = document.getElementById('ocrProcessingText');
    const ocrProcessingText = ocrProcessingTextElem ? ocrProcessingTextElem.textContent.trim() : "Ocr processing";

    document.querySelectorAll('.btn-score').forEach(btn => {
        btn.addEventListener('click', handleTicketScore);
    });

    const initialMessages = Array.from(chatBox.querySelectorAll('.message'));
    initialMessages.forEach(el => el.remove());
    initialMessages.reverse().forEach(el => chatBox.appendChild(el));

    /* ---------------- seen messages ---------------- */
    setTimeout(() => {
        markMessagesAsSeen();
    }, 1500);

    document.querySelectorAll('.message').forEach(applyDirectionToElement);

    /* ---------------- send message ---------------- */
    async function sendMessage() {

        const ticketId = document.getElementById("ticketId").value;
        const content = document.getElementById("messageContent").value.trim();
        const file = fileInput?.files?.[0];

        if (!content && !file) return;

        const formData = new FormData();
        formData.append("content", content);
        if (file) formData.append("files", file);

        try {
            const response = await secureFetch(`/rest/messages/${ticketId}`, {
                method: "POST",
                body: formData
            });

            const message = await handleResponse(response);
            addMessageToDOM(message);

            form.reset();
            chatBox.scrollTop = chatBox.scrollHeight;

        } catch (error) {
            console.error(error);
        }
    }

    /* ---------------- events ---------------- */

    fileInput?.addEventListener('change', async () => {
        if (fileInput.files.length > 0) {
            await sendMessage();
        }
    });

    form?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await sendMessage();
    });

    ocrBtn?.addEventListener('click', async () => {

        const tempInput = document.createElement('input');
        tempInput.type = 'file';
        tempInput.accept = 'image/*,application/pdf';

        tempInput.addEventListener('change', async () => {

            const file = tempInput.files[0];
            if (!file) return;

            const ticketId = document.getElementById("ticketId").value;

            const formData = new FormData();
            formData.append("file", file);

            showToast('info', ocrProcessingText || 'در حال پردازش OCR ...');

            const response = await secureFetch(`/rest/messages/ocr/${ticketId}`, {
                method: "POST",
                body: formData
            });

            const message = await response.json();
            addMessageToDOM(message);

            setTimeout(async () => {
                await reloadMessages();
            }, 5000);

            tempInput.remove();
        });

        document.body.appendChild(tempInput);
        tempInput.click();
    });

    /* ---------------- load older ---------------- */

    loadOlderBtn?.addEventListener('click', loadOlderMessages);

    /* ---------------- ticket close ---------------- */

    ticketCloseBtn?.addEventListener('click', async () => {

        const ticketId = document.getElementById("ticketId").value;

        try {
            const response = await secureFetch(`/rest/messages/ticket-close/${ticketId}`, {
                method: 'PUT'
            });

            const data = await handleResponse(response);

            showToast('success', data.message || 'مکالمه پایان یافت');

        } catch (error) {
            if (error.message !== 'Validation errors') {
                console.error(error);
                showToast('danger', error.message || 'خطا در بستن مکالمه');
            }
        }
    });

});

/* =========================
   FUNCTIONS OUTSIDE DOM
========================= */

function addMessageToDOM(message) {

    const chatBox = document.getElementById('chat-box');
    const msgDiv = createMessageElement(message);

    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
}

async function loadOlderMessages() {

    const ticketId = document.getElementById("ticketId").value;

    const response = await fetch(
        `/rest/messages/${ticketId}?page=${currentPage + 1}&size=${pageSize}`
    );

    if (!response.ok) return;

    const data = await response.json();
    const messages = data.content || data;

    if (!Array.isArray(messages) || messages.length === 0) return;

    currentPage++;

    const chatBox = document.getElementById("chat-box");

    messages.forEach(message => {
        chatBox.insertBefore(
            createMessageElement(message),
            chatBox.firstChild
        );
    });
}

async function markMessagesAsSeen() {

    const ticketId = document.getElementById("ticketId").value;

    try {
        await secureFetch(`/rest/messages/seen/${ticketId}`, {
            method: 'PUT'
        });
    } catch (e) {
        console.error(e);
    }
}


async function handleResponse(response, mode) {
    const data = await response.json();

    if (!response.ok) {
        if (response.status === 400) {
            // خطاهای اعتبارسنجی
            displayValidationErrors(data, mode);
            throw new Error('Validation errors');
        }

        // سایر خطاها (۵۰۰ یا ...)، پیام در فیلد error است
        const errorMessage = data.error || 'خطای ناشناخته در سرور';
        showToast('danger', errorMessage);
        throw new Error(errorMessage);
    }

    // موفقیت (پیام در فیلد message است)
    return data;
}

async function handleTicketScore(e) {

    const score = e.currentTarget.dataset.score;
    const ticketId = document.getElementById("ticketId").value;

    try {
        const response = await secureFetch(`/rest/messages/ticket-score/${ticketId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(score)
        });

        const data = await handleResponse(response);

        showToast('success', data.message || 'امتیاز ثبت شد');

        disableScoreButtons();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error(error);
            showToast('danger', error.message || 'خطا در ثبت امتیاز');
        }
    }
}

function disableScoreButtons() {
    document.querySelectorAll('.btn-score').forEach(btn => {
        btn.disabled = true;
        btn.classList.add('disabled');
    });
}

async function reloadMessages() {

    const ticketId = document.getElementById("ticketId").value;

    const response = await fetch(
        `/rest/messages/${ticketId}?page=0&size=50`
    );

    if (!response.ok) return;

    const data = await response.json();

    const messages = data.content || data;

    const chatBox = document.getElementById('chat-box');

    chatBox.innerHTML = '';

    messages.reverse().forEach(message => {
        chatBox.appendChild(createMessageElement(message));
    });

    chatBox.scrollTop = chatBox.scrollHeight;
}