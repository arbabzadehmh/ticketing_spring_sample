async function handleForgotPassword(e) {
    e.preventDefault();

    const username =
        document.getElementById('username').value.trim();

    const response = await secureFetch(
        '/rest/profiles/forgot-password',
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username
            })
        }
    );

    const data = await handleResponse(response);

    showToast('success', data.message);
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


// -------------------- Handle Server Response --------------------
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

// ---------------------------------------------------------
function debounce(fn, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// -----------------------------------------------------------

document.addEventListener('DOMContentLoaded', () => {

    const form =
        document.getElementById('forgotPasswordForm');

    if(form) {
        form.addEventListener(
            'submit',
            handleForgotPassword
        );
    }

});