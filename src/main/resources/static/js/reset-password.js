
// -------------------- Create Profile --------------------

async function handleResetPassword(e) {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();

    try {
        const response = await secureFetch(`/rest/profiles/reset-password/${username}`, {
            method: 'POST',
        });

        const data = await handleResponse(response, 'create'); // مدیریت پاسخ با متد استاندارد

        // موفقیت
        // bootstrap.Modal.getInstance(document.getElementById('profileRegisterModal')).hide();
        showToast('success', data.message || 'رمز عبور با موفقیت بازیابی شد');
        alert(data.resetPass);

        setTimeout(() => window.location.replace("/login"), 5000);

    } catch (error) {
        // خطاهای اعتبارسنجی قبلاً در handleResponse نمایش داده شدند
        if (error.message !== 'Validation errors') {
            console.error('Password resetting error:', error);
            showToast('danger', error.message || 'خطا در بازیابی رمز عبور');
        }
    }
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


// -------------------- DOMContentLoaded --------------------
document.addEventListener('DOMContentLoaded', () => {


    const resetPassForm = document.getElementById('resetPasswordForm');
    if (resetPassForm) resetPassForm.addEventListener('submit', handleResetPassword);

});
