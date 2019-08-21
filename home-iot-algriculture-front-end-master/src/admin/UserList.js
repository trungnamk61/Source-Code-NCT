import React, {Component} from 'react';
import {deleteUser, getAllUsers} from '../util/APIUtils';
import {Button, ConfigProvider, Empty, Icon, Modal, notification, Popconfirm, Table} from 'antd';
import {DEVICE_LIST_SIZE, USER_LIST_SIZE} from '../constants';
import './UserList.css';
import {formatDateTime} from "../util/Helpers";
import Signup from "../user/signup/Signup";


class UserList extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            users: [],
            page: 0,
            size: 15,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false,
            modalVisible: false
        };
    }

    loadUserList = (page = 0, size = USER_LIST_SIZE) => {
        this.setState({
            isLoading: true
        });

        getAllUsers(page, size)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        users: response.content,
                        page: response.page + 1,
                        size: response.size,
                        totalElements: response.totalElements,
                        totalPages: response.totalPages,
                        last: response.last,
                        isLoading: false
                    })
                }
            }).catch(error => {
            this.setState({
                isLoading: false
            });
            notification["error"]({
                message: 'NCT App',
                description: error.message || "Get all users failed.",
            });
        });
    };

    handleDelete = (id) => {
        deleteUser(id)
            .then(response => {
                if (this._isMounted) {
                    const users = [...this.state.users];
                    this.setState({users: users.filter(item => item.id !== id)});
                    notification["success"]({
                        message: 'NCT App',
                        description: response.message || "Delete user succeeded.",
                    });
                }
            }).catch(error => {
            notification["error"]({
                message: 'NCT App',
                description: error.message || "Delete user failed.",
            });
        });
    };

    showModal = () => {
        this.setState({modalVisible: true});
    };

    handleCancel = () => {
        this.setState({modalVisible: false});
    };

    handleCreate = (event) => {
        const form = this.signUpForm;
        if (form.isFormInvalid()) {
            notification["error"]({
                message: 'NCT App',
                description: "Please make sure all fields are filled correctly.",
            });
            return;
        }
        form.handleSubmit(event);
    };

    onNewUser = ()=>{
        this.loadUserList(this.state.totalElements % DEVICE_LIST_SIZE === 0 ? this.state.totalPages : this.state.totalPages - 1);
        this.setState({modalVisible: false});
    };

    componentWillUnmount() {
        this._isMounted = false;
    }

    componentDidMount() {
        this._isMounted = true;
        this.loadUserList();
    }

    handleLoadMore = (page) => {
        this.loadUserList(page - 1);
    };

    render() {
        const {users} = this.state;

        const columns = [{
            title: 'Username',
            dataIndex: 'username',
            key: 'username',
            width: 70
        }, {
            title: 'Full name',
            dataIndex: 'fullName',
            key: 'fullName',
            width: 70,
            editable: true,
        }, {
            title: 'Email',
            dataIndex: 'email',
            key: 'email',
            width: 70
        }, {
            title: 'Role',
            dataIndex: 'roleId',
            key: 'roleId',
            width: 70,
            editable: true,
            render: (text, record) => {
                switch (record.roleId) {
                    case 1:
                        return "Admin";
                    case 3:
                        return "User";
                    default:
                        return "Unknown";
                }
            }
        }, {
            title: 'Created Date',
            key: 'createdDateTime',
            width: 150,
            render: (text, record) => (
                <span>
                {formatDateTime(record.createTime)}
                </span>)
        }, {
            title: 'Last login',
            key: 'lastLogin',
            width: 150,
            render: (text, record) => (
                <span>
                {record.lastLogin ? formatDateTime(record.lastLogin) : "Never Logged in"}
                </span>)
        }, {
            title: 'Action',
            key: 'action',
            width: 70,
            render: (text, record) =>
                this.state.users.length >= 1 ? (
                    <Popconfirm title="Sure to delete?" onConfirm={() => this.handleDelete(record.id)}>
                        <Button type="link">Delete</Button>
                    </Popconfirm>
                ) : null,
        }];

        return (
            <div>
                <h2 className="header">List of Users</h2>
                <Button type="primary" className="button" htmlType="button" onClick={this.showModal}>
                    <Icon type="plus"/> Add User</Button>
                <Modal
                    visible={this.state.modalVisible} title="Create a new user"
                    onCancel={this.handleCancel} onOk={this.handleCreate} okText="Submit">
                    <Signup isAdmin={true} onRef={ref => (this.signUpForm = ref)} onNewUser={this.onNewUser}/>
                </Modal>
                <ConfigProvider renderEmpty={() => (
                    <Empty description=" No User Found."/>
                )}>
                    <Table {...this.state} rowKey={record => record.id} columns={columns}
                           dataSource={users} size="default"
                           pagination={{
                               onChange: (page) => this.handleLoadMore(page),
                               total: this.state.totalElements, showTotal: (total) => `Total ${total} users`,
                               pageSize: this.state.size, current: this.state.page
                           }}/>
                </ConfigProvider>
            </div>
        );
    }
}

export default UserList;