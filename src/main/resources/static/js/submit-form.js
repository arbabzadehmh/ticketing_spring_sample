async function submitForm(elementId, method, callbackFunction, eventType, url = null) {
    document.getElementById(elementId).addEventListener(eventType, async function (event) {
        event.preventDefault();

        const options = { method: method };

        if (event.target.tagName === "FORM") {
            const form = event.target;
            url = form.action;
            const formData = new FormData(form);
            const jsonObject = {};

            formData.forEach((value, key) => {
                jsonObject[key] = value;
            });

            const jsonString = JSON.stringify(jsonObject);
            options.headers = { 'Content-Type': 'application/json' };
            options.body = jsonString;
        }

        try {
            const response = await fetch(url, options);
            const data = await response.json();

            console.log("body : ", data)

            if (response.ok) {
                showToast("info", data.message);
                await callbackFunction(data.data);

            } else {
                showToast("error", `Error ${response.status}: ${data.message}`);
                console.error("Error Response:", data);
            }
        } catch (error) {
            showToast("error", "Invalid URL or Network Error!");
            console.error("Fetch Error:", error);
        }
    });
}

function showToast(type, text) {
    const toastContainer = document.getElementById('toast-container');

    // Create the toast element
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = text;

    // Create the close button
    const closeButton = document.createElement('button');
    closeButton.className = 'close-btn';
    closeButton.innerHTML = '&times;';
    closeButton.onclick = function() {
        toast.classList.remove('show');
        setTimeout(() => {
            toastContainer.removeChild(toast);
        }, 500); // Match this value with the CSS transition duration
    };

    // Append the close button to the toast
    toast.appendChild(closeButton);

    // Add the toast to the container
    toastContainer.appendChild(toast);

    // Trigger the animation
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);

    // Automatically remove the toast after the duration
    setTimeout(() => {
        if (toastContainer.contains(toast)) {
            toast.classList.remove('show');
            setTimeout(() => {
                if (toastContainer.contains(toast)) {
                    toastContainer.removeChild(toast);
                }
            }, 500); // Match this value with the CSS transition duration
        }
    }, 3000); // Duration of the toast
}
