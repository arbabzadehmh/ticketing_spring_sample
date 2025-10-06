document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('messageForm');
    const chatBox = document.getElementById('chat-box');

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const formData = new FormData();
            formData.append("ticketId", document.getElementById("ticketId").value);
            formData.append("content", document.getElementById("messageContent").value);
            if (document.getElementById("messageFile").files[0]) {
                formData.append("file", document.getElementById("messageFile").files[0]);
            }

            try {
                const response = await fetch("/rest/messages", {
                    method: "POST",
                    body: formData
                });

                if (!response.ok) throw new Error("خطا در ارسال پیام");

                const message = await response.json();

                // اضافه کردن پیام جدید به DOM
                const msgDiv = document.createElement("div");
                msgDiv.className = "message p-2 mb-2 rounded " +
                    (message.senderRoleName === 'ROLE_ADMIN' ? 'msg-admin'
                        : (message.senderRoleName === 'ROLE_MANAGER' ? 'msg-manager' : 'msg-customer'));

                msgDiv.innerHTML = `
                    <p class="mb-1">${message.content}</p>
                    <small class="text-muted">${message.dateTime}</small>
                `;

                chatBox.appendChild(msgDiv);

                // پاک کردن فرم
                form.reset();
                chatBox.scrollTop = chatBox.scrollHeight;

            } catch (error) {
                console.error(error);
                alert("ارسال پیام با مشکل مواجه شد");
            }
        });
    }
});
