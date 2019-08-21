import React, {Component} from 'react';
import {login} from '../../util/APIUtils';
import './Login.css';
import {Link, Redirect} from 'react-router-dom';
import {ACCESS_TOKEN} from '../../constants';

import {Form, Input, Button, Icon, notification} from 'antd';

const FormItem = Form.Item;

class Login extends Component {
    _isMounted = false;

    state = {
        redirectToReferrer: false
    };

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    onLogin = () => {
        this.props.onLogin();
        if (this._isMounted) {
            this.setState(() => ({
                redirectToReferrer: true
            }));
        }
    };

    render() {
        const {from} = this.props.location.state || {from: {pathname: '/'}};

        if (this.state.redirectToReferrer || localStorage.getItem(ACCESS_TOKEN)) {
            return <Redirect to={from}/>
        }

        const AntWrappedLoginForm = Form.create()(LoginForm);
        return (
            <div className="login-container">
                <h1 className="page-title">Login</h1>
                <div className="login-content">
                    <AntWrappedLoginForm onLogin={this.onLogin}/>
                </div>
            </div>
        );
    }
}

class LoginForm extends Component {
    handleSubmit = (event) => {
        event.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                const loginRequest = Object.assign({}, values);
                login(loginRequest)
                    .then(response => {
                        localStorage.setItem(ACCESS_TOKEN, response.accessToken);
                        this.props.onLogin();
                    }).catch(error => {
                    if (error.status === 401) {
                        notification.error({
                            message: 'NCT App',
                            description: 'Your Username or Password is incorrect. Please try again!'
                        });
                    } else {
                        notification.error({
                            message: 'NCT App',
                            description: error.message || 'Sorry! Something went wrong. Please try again!'
                        });
                    }
                });
            }
        });
    };

    render() {
        const {getFieldDecorator} = this.props.form;
        return (
            <Form onSubmit={this.handleSubmit} className="login-form">
                <FormItem>
                    {getFieldDecorator('usernameOrEmail', {
                        rules: [{required: true, message: 'Please input your username or email!'}],
                    })(
                        <Input
                            prefix={<Icon type="user"/>}
                            size="large"
                            name="usernameOrEmail"
                            placeholder="Username or Email"/>
                    )}
                </FormItem>
                <FormItem>
                    {getFieldDecorator('password', {
                        rules: [{required: true, message: 'Please input your Password!'}],
                    })(
                        <Input
                            prefix={<Icon type="lock"/>}
                            size="large"
                            name="password"
                            type="password"
                            placeholder="Password"/>
                    )}
                </FormItem>
                <FormItem>
                    <Button type="primary" htmlType="submit" size="large" className="login-form-button">Login</Button>
                    Or <Link to="/signup">register now!</Link>
                </FormItem>
            </Form>
        );
    }
}

export default Login;