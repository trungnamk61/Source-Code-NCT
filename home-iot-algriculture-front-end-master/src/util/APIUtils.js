import {API_BASE_URL, DEVICE_LIST_SIZE, ACCESS_TOKEN, USER_LIST_SIZE} from '../constants';

const request = (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    });

    if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

    return fetch(options.url, options)
        .then(response =>
            response.json().then(json => {
                if (!response.ok) {
                    return Promise.reject(json);
                }
                return json;
            })
        );
};

export function getAllDevicesByUser(page, size, available) {
    page = page || 0;
    size = size || DEVICE_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/device?page=" + page + "&size=" + size + (available ? "&available=" + available : ""),
        method: 'GET'
    });
}

export function getAllDevices(page, size) {
    page = page || 0;
    size = size || DEVICE_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/admin/device?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getAllUsers(page, size) {
    page = page || 0;
    size = size || USER_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/admin/user?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function sendCommand(commandRequest) {
    return request({
        url: API_BASE_URL + "/device/control",
        method: 'POST',
        body: JSON.stringify(commandRequest)
    });
}

export function getDeviceDetails(deviceId) {
    return request({
        url: API_BASE_URL + "/device/" + deviceId,
        method: 'GET'
    });
}

export function getPlantList() {
    return request({
        url: API_BASE_URL + "/plant",
        method: 'GET'
    });
}

export function getAllCrops(page, size) {
    page = page || 0;
    size = size || DEVICE_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/crop?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getCropDetails(cropId) {
    return request({
        url: API_BASE_URL + "/crop/" + cropId,
        method: 'GET'
    });
}

export function getSensorData(cropId) {
    return request({
        url: API_BASE_URL + "/sensor?cropId=" + cropId,
        method: 'GET'
    });
}

export function stopCrop(cropId) {
    return request({
        url: API_BASE_URL + "/crop/stop?cropId=" + cropId,
        method: 'POST'
    });
}

export function createCrop(cropData) {
    return request({
        url: API_BASE_URL + "/crop",
        method: 'POST',
        body: JSON.stringify(cropData)
    });
}

export function deleteCrop(cropId) {
    return request({
        url: API_BASE_URL + "/crop?cropId=" + cropId,
        method: 'DELETE',
    });
}

export function deleteUser(userId) {
    return request({
        url: API_BASE_URL + "/admin/user?userId=" + userId,
        method: 'DELETE',
    });
}

export function createDevice(deviceData) {
    return request({
        url: API_BASE_URL + "/admin/device",
        method: 'POST',
        body: JSON.stringify(deviceData)
    });
}

export function deleteDevice(deviceId) {
    return request({
        url: API_BASE_URL + "/admin/device?deviceId=" + deviceId,
        method: 'DELETE',
    });
}

export function login(loginRequest) {
    return request({
        url: API_BASE_URL + "/auth/signin",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function signup(signupRequest, isAdmin) {
    return request({
        url: API_BASE_URL + (isAdmin ? "/admin/user" : "/auth/signup"),
        method: 'POST',
        body: JSON.stringify(signupRequest)
    });
}

export function checkUsernameAvailability(username) {
    return request({
        url: API_BASE_URL + "/user/checkUsernameAvailability?username=" + username,
        method: 'GET'
    });
}

export function checkEmailAvailability(email) {
    return request({
        url: API_BASE_URL + "/user/checkEmailAvailability?email=" + email,
        method: 'GET'
    });
}


export function getCurrentUser() {
    if (!localStorage.getItem(ACCESS_TOKEN)) {
        return Promise.reject("No access token set.");
    }

    return request({
        url: API_BASE_URL + "/user/me",
        method: 'GET'
    });
}

export function getUserProfile(username) {
    return request({
        url: API_BASE_URL + "/users/" + username,
        method: 'GET'
    });
}

const requestESP = (options) => {
    return new Promise(function (resolve, reject) {
        var xhr = new XMLHttpRequest();
        xhr.open(options.method, options.url);
        xhr.onload = function () {
            if (this.status >= 200 && this.status < 300) {
                resolve(xhr.response);
            } else {
                reject({
                    status: this.status,
                    statusText: xhr.statusText
                });
            }
        };
        xhr.onerror = function () {
            reject({
                status: this.status,
                statusText: xhr.statusText
            });
        };
        xhr.send();
    });
};


export function getStatus() {
    return requestESP({
        url: "http://192.168.1.1/status.json",
        method: 'GET'
    });
}