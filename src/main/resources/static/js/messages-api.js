document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('messageForm');
    const chatBox = document.getElementById('chat-box');
    const fileInput = document.getElementById('messageFile');

    // -------------------- ارسال پیام (متن یا فایل) --------------------
    async function sendMessage() {
        const ticketId = document.getElementById("ticketId").value;
        const content = document.getElementById("messageContent").value.trim();
        const file = fileInput.files[0];

        // اگر نه متن و نه فایل موجود باشد، ارسال نکن
        if (!content && !file) return;

        const formData = new FormData();
        formData.append("content", content);
        if (file) formData.append("files", file);

        try {
            const response = await fetch(`/rest/messages/${ticketId}`, {
                method: "POST",
                body: formData
            });

            const message = await handleResponse(response, 'message');

            let formattedDate = Array.isArray(message.dateTime)
                ? formatDateTime(message.dateTime)
                : message.dateTime;

            // -------------------- اضافه کردن پیام به DOM --------------------
            const msgDiv = document.createElement("div");
            msgDiv.className = "message p-2 mb-2 rounded " +
                (message.senderRoleName === 'ROLE_ADMIN' ? 'msg-admin'
                    : (message.senderRoleName === 'ROLE_MANAGER' ? 'msg-manager' : 'msg-customer'));

            let html = "";

            html += `<h6 class="text-muted d-block mt-1 ltr small-text">${message.senderUsername}:</h6>`;

            // متن پیام
            if (message.content) {
                html += `<p class="mb-1">${message.content}</p>`;
            }

            // فایل‌ها
            if (message.attachments && message.attachments.length > 0) {
                html += `<div class="attachments mt-2">`;
                message.attachments.forEach(att => {
                    const downloadUrl = `/rest/images/${att.mongoFileId}/download`;
                    const rawUrl = `/rest/images/${att.mongoFileId}/raw`;

                    if (['JPG', 'PNG', 'BMP'].includes(att.fileType)) {
                        html += `<a href="${rawUrl}" target="_blank">
                                    <img src="${rawUrl}" class="chat-thumb" />
                                </a>`;
                    } else if (att.fileType === 'PDF') {
                        html += `<a href="${downloadUrl}" class="d-block" target="_blank">
                                    <i class="fas fa-file-pdf fa-2x text-danger"></i>
                                    <small>${att.fileName}</small>
                                </a>`;
                    } else {
                        html += `<a href="${downloadUrl}" target="_blank">
                                    <i class="fas fa-file fa-2x"></i>
                                    <small>${att.fileName}</small>
                                </a>`;
                    }
                });
                html += `</div>`;
            }

            // تاریخ پیام
            html += `<small class="text-muted d-block mt-1 ltr small-text">${formattedDate}</small>`;
            msgDiv.innerHTML = html;

            chatBox.appendChild(msgDiv);

            applyDirectionToElement(msgDiv);

            // پاک کردن فرم و اسکرول به پایین
            form.reset();
            chatBox.scrollTop = chatBox.scrollHeight;

        } catch (error) {
            console.error('Message sending error:', error);
            showToast('danger', error.message || 'خطا در ارسال پیام');
        }
    }

    // -------------------- listener فرم submit --------------------
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await sendMessage();
        });
    }

    // -------------------- listener تغییر فایل --------------------
    if (fileInput) {
        fileInput.addEventListener('change', async () => {
            if (fileInput.files.length > 0) {
                await sendMessage();
            }
        });
    }
});

// -------------------- قالب‌بندی تاریخ --------------------
function formatDateTime(dtArray) {
    let [year, month, day, hour, minute] = dtArray;
    month = month.toString().padStart(2, "0");
    day = day.toString().padStart(2, "0");
    hour = hour.toString().padStart(2, "0");
    minute = minute.toString().padStart(2, "0");
    return `${year}-${month}-${day} ${hour}:${minute}`;
}

// -------------------- هندل پاسخ سرور --------------------
async function handleResponse(response, mode) {
    const data = await response.json();

    if (!response.ok) {
        if (response.status === 400) {
            displayValidationErrors(data, mode);
            throw new Error('Validation errors');
        }
        const errorMessage = data.error || data.message || 'خطای ناشناخته در سرور';
        showToast('danger', errorMessage);
        throw new Error(errorMessage);
    }
    return data;
}

// ------------------------------------------------------

function isRTL(text) {
    const persianRegex = /[\u0600-\u06FF]/;  // بازه یونیکد فارسی/عربی
    return persianRegex.test(text);
}

function applyDirection() {
    document.querySelectorAll('.message p').forEach(p => {
        const text = p.innerText.trim();
        const parent = p.closest('.message');

        if (isRTL(text)) {
            parent.classList.add('rtl');
            parent.classList.remove('ltr');
        } else {
            parent.classList.add('ltr');
            parent.classList.remove('rtl');
        }
    });
}

// اجرا پس از لود صفحه
document.addEventListener('DOMContentLoaded', applyDirection);

// ---------------------------------------------------------

const input = document.getElementById("messageContent");

input.addEventListener("input", () => {
    const val = input.value;
    const persianRegex = /[\u0600-\u06FF]/;

    if (persianRegex.test(val)) {
        input.style.direction = "rtl";
        input.style.textAlign = "right";
    } else {
        input.style.direction = "ltr";
        input.style.textAlign = "left";
    }
});

// ----------------------------------------------------------------

function applyDirectionToElement(el) {
    const p = el.querySelector("p");
    if (!p) return;

    const text = p.innerText.trim();

    if (isRTL(text)) {
        el.classList.add("rtl");
        el.classList.remove("ltr");
    } else {
        el.classList.add("ltr");
        el.classList.remove("rtl");
    }
}
