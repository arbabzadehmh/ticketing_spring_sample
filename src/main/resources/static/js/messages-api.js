document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('messageForm');
    const chatBox = document.getElementById('chat-box');
    const fileInput = document.getElementById('messageFile');
    const ocrBtn = document.getElementById('ocrSendBtn');

    document.querySelectorAll('.message').forEach(msgDiv => {
        applyDirectionToElement(msgDiv);
    });

    // -------------------- ارسال پیام (متن یا فایل) --------------------
    async function sendMessage() {
        const ticketId = document.getElementById("ticketId").value;
        const content = document.getElementById("messageContent").value.trim();
        const file = fileInput.files[0];

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
            addMessageToDOM(message);

            form.reset();
            chatBox.scrollTop = chatBox.scrollHeight;
        } catch (error) {
            console.error('Message sending error:', error);
            showToast('danger', error.message || 'خطا در ارسال پیام');
        }
    }


    // -------------------- ارسال خودکار بعد از انتخاب فایل --------------------
    if (fileInput) {
        fileInput.addEventListener('change', async () => {
            if (fileInput.files.length > 0) {
                await sendMessage(); // ارسال خودکار پیام معمولی
            }
        });
    }

    // -------------------- listener فرم submit --------------------
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await sendMessage();
        });
    }

    // -------------------- دکمه OCR --------------------
    if (ocrBtn) {
        ocrBtn.addEventListener('click', async () => {
            // ایجاد یک input file موقت
            const tempInput = document.createElement('input');
            tempInput.type = 'file';
            tempInput.accept = 'image/*,application/pdf';
            tempInput.style.display = 'none';

            // اضافه کردن listener برای تغییر فایل (بعد از انتخاب فایل)
            tempInput.addEventListener('change', async () => {
                const file = tempInput.files[0];
                if (!file) return;

                const ticketId = document.getElementById("ticketId").value;
                const formData = new FormData();
                formData.append("file", file);

                try {
                    const response = await fetch(`/rest/messages/ocr/${ticketId}`, {
                        method: "POST",
                        body: formData
                    });

                    const message = await response.json();
                    addMessageToDOM(message); // تابعی که پیام را به چت اضافه می‌کند
                } catch (error) {
                    console.error(error);
                    showToast("danger", error.message || "خطا در ارسال فایل برای OCR");
                }
            });

            document.body.appendChild(tempInput);
            tempInput.click(); // باز کردن دیالوگ انتخاب فایل
            tempInput.remove(); // پاک کردن input بعد از trigger
        });
    }

// -------------------- اضافه کردن پیام به DOM --------------------
    function addMessageToDOM(message) {
        const chatBox = document.getElementById('chat-box');
        const msgDiv = document.createElement("div");
        const extractedLabel = document.getElementById("ocrExtractedTextLabel")?.innerText || "OCR Text:";

        msgDiv.className = "message p-2 mb-2 rounded " +
            (message.senderRoleName === 'ROLE_ADMIN' ? 'msg-admin'
                : (message.senderRoleName === 'ROLE_MANAGER' ? 'msg-manager' : 'msg-customer'));

        let html = `<h6 class="text-muted d-block mt-1 ltr small-text">${message.senderUsername}:</h6>`;
        if (message.content) html += `<p class="mb-1">${message.content}</p>`;

        if (message.attachments &&
            message.attachments.length > 0 &&
            message.attachments[0].extractedText) {

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
                    html += `<a href="${rawUrl}" target="_blank"><img src="${rawUrl}" class="chat-thumb"/></a>`;
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

        const formattedDate = Array.isArray(message.dateTime)
            ? formatDateTime(message.dateTime)
            : message.dateTime;
        html += `<small class="text-muted d-block mt-1 ltr small-text">${formattedDate}</small>`;
        msgDiv.innerHTML = html;

        chatBox.appendChild(msgDiv);
        applyDirectionToElement(msgDiv);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

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

// -------------------- RTL/LTR --------------------
    function isRTL(text) {
        const persianRegex = /[\u0600-\u06FF]/;
        return persianRegex.test(text);
    }

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

// -------------------- جهت ورودی --------------------
    const input = document.getElementById("messageContent");
    if (input) {
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
    }
});

