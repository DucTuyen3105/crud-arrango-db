const userForm = document.getElementById("userForm");
const userTableBody = document.getElementById("userTableBody");
const userIdInput = document.getElementById("userId");
const nameInput = document.getElementById("name");
const emailInput = document.getElementById("email");
const ageInput = document.getElementById("age");
const formTitle = document.getElementById("formTitle");
const submitBtn = document.getElementById("submitBtn");
const cancelEditBtn = document.getElementById("cancelEditBtn");
const messageEl = document.getElementById("message");

async function fetchUsers() {
    const response = await fetch("/api/users");
    const users = await response.json();
    renderUsers(users);
}

function renderUsers(users) {
    userTableBody.innerHTML = "";

    users.forEach((user) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${user.key ?? ""}</td>
            <td>${user.name ?? ""}</td>
            <td>${user.email ?? ""}</td>
            <td>${user.age ?? ""}</td>
            <td>
                <button data-action="edit" data-id="${user.key}">Edit</button>
                <button class="danger" data-action="delete" data-id="${user.key}">Delete</button>
            </td>
        `;
        userTableBody.appendChild(row);
    });
}

function showMessage(text, isError = false) {
    messageEl.textContent = text;
    messageEl.style.color = isError ? "#cc2f2f" : "#167b2f";
}

function resetForm() {
    userIdInput.value = "";
    userForm.reset();
    formTitle.textContent = "Create User";
    submitBtn.textContent = "Create";
    cancelEditBtn.classList.add("hidden");
}

async function createUser(payload) {
    const response = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(JSON.stringify(error));
    }
    showMessage("User created successfully");
}

async function updateUser(id, payload) {
    const response = await fetch(`/api/users/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(JSON.stringify(error));
    }
    showMessage("User updated successfully");
}

async function deleteUser(id) {
    const response = await fetch(`/api/users/${id}`, {
        method: "DELETE"
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(JSON.stringify(error));
    }
    showMessage("User deleted successfully");
}

userForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const payload = {
        name: nameInput.value.trim(),
        email: emailInput.value.trim(),
        age: Number(ageInput.value)
    };

    try {
        if (userIdInput.value) {
            await updateUser(userIdInput.value, payload);
        } else {
            await createUser(payload);
        }
        resetForm();
        await fetchUsers();
    } catch (error) {
        showMessage(`Error: ${error.message}`, true);
    }
});

cancelEditBtn.addEventListener("click", () => {
    resetForm();
    showMessage("");
});

userTableBody.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
        return;
    }

    const action = target.dataset.action;
    const id = target.dataset.id;
    if (!action || !id) {
        return;
    }

    if (action === "delete") {
        if (!window.confirm("Delete this user?")) {
            return;
        }
        try {
            await deleteUser(id);
            await fetchUsers();
        } catch (error) {
            showMessage(`Error: ${error.message}`, true);
        }
        return;
    }

    if (action === "edit") {
        try {
            const response = await fetch(`/api/users/${id}`);
            if (!response.ok) {
                throw new Error("Cannot find user");
            }
            const user = await response.json();
            userIdInput.value = user.key;
            nameInput.value = user.name ?? "";
            emailInput.value = user.email ?? "";
            ageInput.value = user.age ?? 0;
            formTitle.textContent = "Edit User";
            submitBtn.textContent = "Update";
            cancelEditBtn.classList.remove("hidden");
            showMessage("");
        } catch (error) {
            showMessage(`Error: ${error.message}`, true);
        }
    }
});

fetchUsers().catch((error) => showMessage(`Error: ${error.message}`, true));
