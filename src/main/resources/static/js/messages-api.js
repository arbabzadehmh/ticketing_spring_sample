document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('messageForm');
    const chatBox = document.getElementById('chat-box');

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const ticketId = document.getElementById("ticketId").value;

            const formData = new FormData();

            formData.append("content", document.getElementById("messageContent").value);

            const fileInput = document.getElementById("messageFile").files[0];
            if (fileInput) {
                formData.append("files", fileInput);
            }

            try {
                const response = await fetch(`/rest/messages/${ticketId}`, {
                    method: "POST",
                    body: formData
                });

                const message = await handleResponse(response, 'message');

                let formattedDate = "";
                if (Array.isArray(message.dateTime)) {
                    formattedDate = formatDateTime(message.dateTime);
                } else {
                    formattedDate = message.dateTime;
                }

                // اضافه کردن پیام جدید به DOM
                const msgDiv = document.createElement("div");
                msgDiv.className = "message p-2 mb-2 rounded " +
                    (message.senderRoleName === 'ROLE_ADMIN' ? 'msg-admin'
                        : (message.senderRoleName === 'ROLE_MANAGER' ? 'msg-manager' : 'msg-customer'));

                msgDiv.innerHTML = `
                    <p class="mb-1">${message.content}</p>
                    <small class="text-muted">${formattedDate}</small>
                `;

                chatBox.appendChild(msgDiv);

                // پاک کردن فرم
                form.reset();
                chatBox.scrollTop = chatBox.scrollHeight;

            } catch (error) {
                console.error('Message sending error:', error);
                showToast('danger', error.message || 'خطا در ارسال پیام');
            }
        });
    }
});

function formatDateTime(dtArray) {
    // ورودی مثل: [2025,11,17,18,46,45,545261200]
    let [year, month, day, hour, minute] = dtArray;
    month = month.toString().padStart(2, "0");
    day = day.toString().padStart(2, "0");
    hour = hour.toString().padStart(2, "0");
    minute = minute.toString().padStart(2, "0");

    return `${year}-${month}-${day} ${hour}:${minute}`;
}


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
