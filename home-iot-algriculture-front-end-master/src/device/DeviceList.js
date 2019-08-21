import React, {Component} from 'react';
import {Button} from 'antd';
import './DeviceList.css';
import DeviceTable from "./DeviceTable";

class DeviceList extends Component {
    render() {
        return (
            <div>
                <h2 className="header">List of devices</h2>
                <Button type="primary" htmlType="button" className="button"
                        onClick={() => this.props.history.push("/device/new")}>+ New Device</Button>
                <DeviceTable {...this.props}/>
            </div>
        );
    }
}

export default DeviceList;