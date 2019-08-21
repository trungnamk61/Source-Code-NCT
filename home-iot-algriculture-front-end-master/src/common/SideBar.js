import {Layout, Menu, Icon} from 'antd';
import {Component} from "react";
import {Link, withRouter} from "react-router-dom";
import React from "react";
import './SideBar.css';
import {Role} from "../constants";

const {Sider} = Layout;

class SideBar extends Component {
    render() {
        let menuItems;
        if (this.props.currentUser) {
            menuItems = [
                <Menu.Item key="/profile" className="profile-menu" title="Profile">
                    <Link to={`/users/${this.props.currentUser.username}`}>
                        <span><Icon type="user"/><span>Hello, {this.props.currentUser.name}</span></span>
                    </Link>
                </Menu.Item>,
                <Menu.Item key="/">
                    <Link to="/">
                        <Icon type="home" className="nav-icon"/><span>Home</span>
                    </Link>
                </Menu.Item>
            ];
            if (this.props.currentUser.role === Role.User) {
                menuItems.push([
                    <Menu.Item key="/crop">
                        <Link to="/crop">
                            <Icon type="appstore" className="nav-icon"/><span>Crop</span>
                        </Link>
                    </Menu.Item>,
                    <Menu.Item key="/device">
                        <Link to="/device">
                            <Icon type="box-plot" className="nav-icon"/><span>Devices</span>
                        </Link>
                    </Menu.Item>
                ]);
            } else if (this.props.currentUser.role === Role.Admin) {
                menuItems.push([
                    <Menu.Item key="/user-manager">
                        <Link to="/user-manager">
                            <Icon type="usergroup-add" className="nav-icon"/><span>User Manager</span>
                        </Link>
                    </Menu.Item>,
                    <Menu.Item key="/device-manager">
                        <Link to="/device-manager">
                            <Icon type="box-plot" className="nav-icon"/><span>Device Manager</span>
                        </Link>
                    </Menu.Item>
                ]);
            }
        } else {
            menuItems = [
                <Menu.Item key="/login">
                    <Link to="/login">Login</Link>
                </Menu.Item>,
                <Menu.Item key="/signup">
                    <Link to="/signup">Signup</Link>
                </Menu.Item>
            ];
        }


        return (
            <Sider
                collapsible
                breakpoint="lg"
                collapsedWidth="0"
            >
                <div className="logo"/>
                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[this.props.location.pathname]}>
                    {menuItems}
                </Menu>
            </Sider>
        )
    }
}

export default withRouter(SideBar);