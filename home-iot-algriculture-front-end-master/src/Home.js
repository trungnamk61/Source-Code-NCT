import React, {Component} from 'react';
import './app/App.css';
import {Button, Layout} from 'antd';
import {withRouter} from "react-router-dom";

const {Content} = Layout;

class Home extends Component {
    render() {
        const message = this.props.isAuthenticated ?
            <h2>Welcome, {this.props.currentUser.name}!</h2> :
            <p>Please log in to manage your NCT Agriculture.</p>;

        const button = this.props.isAuthenticated ?
            <div>
                <Button color="link" htmlType="button" onClick={this.props.onLogout}>Logout</Button>
            </div> :
            <Button color="primary" htmlType="button" onClick={() => this.props.history.push('/login')}>Login</Button>;

        return (
            <Content className="app-content">
                {message}
                {button}
            </Content>
        );
    }
}

export default withRouter(Home);