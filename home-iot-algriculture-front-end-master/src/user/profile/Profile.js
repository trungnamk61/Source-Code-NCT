import React, {Component} from 'react';
import {getUserProfile} from '../../util/APIUtils';
import {Avatar, Button} from 'antd';
import {getAvatarColor} from '../../util/Colors';
import {formatDate} from '../../util/Helpers';
import LoadingIndicator from '../../common/LoadingIndicator';
import './Profile.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';

class Profile extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            user: null,
            isLoading: false
        };
    }

    loadUserProfile = (username) => {
        this.setState({
            isLoading: true
        });

        getUserProfile(username)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        user: response,
                        isLoading: false
                    });
                }
            }).catch(error => {
            if (error.status === 404) {
                this.setState({
                    notFound: true,
                    isLoading: false
                });
            } else {
                this.setState({
                    serverError: true,
                    isLoading: false
                });
            }
        });
    };

    onLogout = () => {
        this.props.onLogout();
    };

    componentDidMount() {
        this._isMounted = true;

        const username = this.props.match.params.username;
        this.loadUserProfile(username);
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    componentDidUpdate(nextProps) {
        if (this.props.match.params.username !== nextProps.match.params.username) {
            this.loadUserProfile(nextProps.match.params.username);
        }
    }

    render() {
        if (this.state.isLoading) {
            return <LoadingIndicator/>;
        }

        if (this.state.notFound) {
            return <NotFound/>;
        }

        if (this.state.serverError) {
            return <ServerError/>;
        }

        return (
            <div className="profile">
                {
                    this.state.user ? (
                        <div className="user-profile">
                            <div className="user-details">
                                <div className="user-avatar">
                                    <Avatar className="user-avatar-circle"
                                            style={{backgroundColor: getAvatarColor(this.state.user.name)}}>
                                        {this.state.user.name[0].toUpperCase()}
                                    </Avatar>
                                </div>
                                <div className="user-summary">
                                    <div className="full-name">{this.state.user.name}</div>
                                    <div className="username">@{this.state.user.username}</div>
                                    <div className="user-joined">
                                        Joined {formatDate(this.state.user.joinedAt)}
                                    </div>
                                    <div>
                                        {
                                            this.props.isAuthenticated
                                            && this.props.currentUser.username === this.state.user.username ? (
                                                <Button onClick={this.onLogout} className="logout" key="logout"
                                                        type="danger" size="large" htmlType="button">Logout</Button>
                                            ) : null
                                        }
                                    </div>
                                </div>
                            </div>
                        </div>
                    ) : null
                }
            </div>
        );
    }
}

export default Profile;