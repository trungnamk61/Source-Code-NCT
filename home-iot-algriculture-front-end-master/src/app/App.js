import React, {Component} from 'react';
import './App.css';
import {Route, withRouter, Switch} from 'react-router-dom';

import {getCurrentUser} from '../util/APIUtils';
import {ACCESS_TOKEN, ROLE, Role} from '../constants';

import SideBar from "../common/SideBar";
import AppHeader from "../common/AppHeader";
import NotFound from "../common/NotFound";
import Signup from "../user/signup/Signup";

import Login from "../user/login/Login";
import Profile from "../user/profile/Profile";
import LoadingIndicator from "../common/LoadingIndicator";

import {Layout, notification} from "antd";
import Home from "../Home";
import PrivateRoute from "../common/PrivateRoute";
import DeviceList from "../device/DeviceList";
import NewDevice from "../device/NewDevice";
import NewCrop from "../crop/NewCrop";
import CropList from "../crop/CropList";
import CropStat from "../crop/CropStat";
import DeviceDetails from "../device/DeviceDetails";
import UserList from "../admin/UserList";
import DeviceManagerList from "../admin/DeviceManagerList";

class App extends Component {

    constructor(props) {
        super(props);
        this.state = {
            currentUser: null,
            isAuthenticated: false,
            isLoading: false
        };

        notification.config({
            placement: 'topRight',
            top: 70,
            duration: 3,
        });
    }

    loadCurrentUser = () => {
        if (localStorage.getItem(ACCESS_TOKEN) && !this.state.isAuthenticated) {
            this.setState({
                isLoading: true
            });
            getCurrentUser()
                .then(response => {
                    localStorage.setItem(ROLE, response.role);
                    this.setState({
                        currentUser: response,
                        isAuthenticated: true,
                        isLoading: false
                    });
                }).catch(error => {
                localStorage.removeItem(ACCESS_TOKEN);
                localStorage.removeItem(ROLE);
                this.setState({
                    isLoading: false
                });
                notification["error"]({
                    message: 'NCT App',
                    description: error.message || "Your token may expired. Please login again",
                });
            });
        }
    };

    componentDidMount() {
        this.loadCurrentUser();
    }

    handleLogout = (redirectTo = "/", notificationType = "success", description = "You're successfully logged out.") => {
        localStorage.removeItem(ACCESS_TOKEN);
        localStorage.removeItem(ROLE);

        this.setState({
            currentUser: null,
            isAuthenticated: false
        });

        this.props.history.push(redirectTo);

        notification[notificationType]({
            message: 'NCT App',
            description: description,
        });
    };

    handleLogin = () => {
        notification.success({
            message: 'NCT App',
            description: "You're successfully logged in.",
        });
        this.loadCurrentUser();
    };

    render() {
        if (this.state.isLoading) {
            return <LoadingIndicator/>
        }
        return (
            <Layout className="app-container">
                <SideBar isAuthenticated={this.state.isAuthenticated}
                         currentUser={this.state.currentUser}/>
                <Layout>
                    <AppHeader isAuthenticated={this.state.isAuthenticated}
                               currentUser={this.state.currentUser}/>
                    <div className="app-content">
                        <div className="container">
                            <Switch>
                                <Route exact path="/"
                                       render={(props) => <Home isAuthenticated={this.state.isAuthenticated}
                                                                currentUser={this.state.currentUser}
                                                                onLogout={this.handleLogout} {...props} />}/>
                                <Route path="/login"
                                       render={(props) => <Login onLogin={this.handleLogin} {...props} />}/>
                                <Route path="/signup" component={Signup}/>
                                <Route path="/users/:username"
                                       render={(props) => <Profile isAuthenticated={this.state.isAuthenticated}
                                                                   currentUser={this.state.currentUser}
                                                                   onLogout={this.handleLogout} {...props}  />}>
                                </Route>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/device/new"
                                              component={NewDevice} roles={Role.User} currentUser={this.state.currentUser}/>
                                <PrivateRoute path="/device/:deviceId" authenticated={this.state.isAuthenticated}
                                              component={DeviceDetails} roles={Role.User} currentUser={this.state.currentUser}/>
                                <PrivateRoute path="/device" authenticated={this.state.isAuthenticated}
                                              component={DeviceList} roles={Role.User} currentUser={this.state.currentUser}/>
                                <PrivateRoute authenticated={this.state.isAuthenticated} path="/crop/new"
                                              component={NewCrop} roles={Role.User} currentUser={this.state.currentUser}/>
                                <PrivateRoute path="/crop/:cropId" authenticated={this.state.isAuthenticated}
                                              component={CropStat} roles={Role.User} currentUser={this.state.currentUser}/>
                                <PrivateRoute path="/crop" authenticated={this.state.isAuthenticated}
                                              component={CropList} roles={Role.User} currentUser={this.state.currentUser}/>
                                <PrivateRoute path="/user-manager" authenticated={this.state.isAuthenticated}
                                              component={UserList} roles={Role.Admin} currentUser={this.state.currentUser}/>
                                <PrivateRoute path="/device-manager" authenticated={this.state.isAuthenticated}
                                              component={DeviceManagerList} roles={Role.Admin} currentUser={this.state.currentUser}/>
                                <Route component={NotFound}/>
                            </Switch>
                        </div>
                    </div>
                </Layout>
            </Layout>
        )
    }
}

export default withRouter(App);