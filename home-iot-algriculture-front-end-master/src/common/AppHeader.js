import React, {Component} from 'react';
import {Link, withRouter} from 'react-router-dom';
import './AppHeader.css';
import {Layout} from 'antd';

const Header = Layout.Header;

class AppHeader extends Component {
    render() {
        return (
            <Header className="app-header">
                <div className="container">
                    <div className="app-title">
                        <Link to="/">NCT Agriculture</Link>
                    </div>
                </div>
            </Header>
        );
    }
}

export default withRouter(AppHeader);