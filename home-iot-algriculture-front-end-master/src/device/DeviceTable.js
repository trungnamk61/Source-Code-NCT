import React, {Component} from 'react';
import {getAllDevicesByUser} from '../util/APIUtils';
import {Button, ConfigProvider, Empty, notification, Table} from 'antd';
import {DEVICE_LIST_SIZE} from '../constants';
import './DeviceList.css';
import {formatDateTime} from "../util/Helpers";

class DeviceTable extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            devices: [],
            page: 0,
            size: 15,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
    }

    loadDeviceList = (page = 0, size = DEVICE_LIST_SIZE) => {
        this.setState({
            isLoading: true
        });
        const available = this.props.selectable ? true : null;

        getAllDevicesByUser(page, size, available)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        devices: response.content,
                        page: response.page,
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
                description: error.message || "Get all devices failed.",
            });
        });
    };

    componentWillUnmount() {
        this._isMounted = false;
    }

    componentDidMount() {
        this._isMounted = true;
        this.loadDeviceList();
    }

    handleLoadMore = (page) => {
        this.loadDeviceList(page - 1);
    };

    onSelectDeviceChange = (selectedRowKeys) => {
        this.props.onSelectDeviceChange(selectedRowKeys);
    };

    render() {
        const {devices} = this.state;
        const {selectable} = this.props;
        const rowSelection = {
            type: "radio",
            onChange: this.onSelectDeviceChange,
        };

        const columns = [{
            title: 'Device Name',
            dataIndex: 'device-name',
            key: 'id',
            width: 70,
            render: (text, record) => (
                <span>
                    {<Button type="link"
                             onClick={() => this.props.history.push('/device/' + record.id, record.crop)}>{"ESP32_ID_" + record.id}
                    </Button>}
                </span>
            ),
        }, {
            title: 'Status',
            dataIndex: 'status',
            key: 'alive',
            width: 10,
            render: (text, record) => (
                <span>
                    {record.alive ? "Collecting" : record.crop ? "Disconnected" : "Available"}
                </span>
            ),
        }, {
            title: 'Current crop',
            dataIndex: 'crop',
            key: 'crop',
            width: 100,
            render: (text, record) => (
                <span>
                    {record.crop ?
                        <Button type="link"
                                onClick={() => this.props.history.push("/crop/" + record.crop.id)}>{record.crop.name}
                        </Button> : "Not register yet"}
                </span>
            ),
        }, {
            title: 'Created Date',
            key: 'creationDateTime',
            width: 150,
            render: (text, record) => (
                <span>
                {formatDateTime(record.creationDateTime)}
                </span>)
        }, {
            title: 'Updated Date',
            key: 'lastUpdateDateTime',
            width: 150,
            render: (text, record) => (
                <span>
                {formatDateTime(record.lastUpdateDateTime)}
                </span>)
        }];

        return (
            <ConfigProvider renderEmpty={() => (
                <Empty description=" No Device Found.">
                    <Button type="primary" htmlType="button" onClick={() => this.props.history.push("/device/new")}>
                        + Add Device</Button>
                </Empty>
            )}>
                <Table {...this.state} rowKey={record => record.id} columns={columns}
                       dataSource={devices} size="default"
                       rowSelection={selectable ? rowSelection : null}
                       pagination={{
                           onChange: (page) => this.handleLoadMore(page),
                           total: this.state.totalElements, showTotal: (total) => `Total ${total} items`,
                           pageSize: this.state.size
                       }}/>
            </ConfigProvider>
        );
    }
}

export default DeviceTable;