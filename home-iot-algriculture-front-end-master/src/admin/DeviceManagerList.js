import React, {Component} from 'react';
import {createDevice, deleteDevice, getAllDevices} from '../util/APIUtils';
import {
    Button,
    Col,
    ConfigProvider,
    Drawer,
    Empty,
    Form,
    Icon,
    notification,
    Popconfirm,
    Row,
    Table,
    Checkbox
} from 'antd';
import {
    EC_SENSOR, FAN,
    HUMIDITY_SENSOR, LED,
    LIGHT_SENSOR,
    PH_SENSOR, PUMP_A, PUMP_B, PUMP_PH_DOWN, PUMP_PH_UP, PUMP_UP, PUMP_WATER,
    TEMPERATURE_SENSOR,
    DEVICE_LIST_SIZE
} from '../constants';
import './DeviceManagerList.css';
import {formatDateTime} from "../util/Helpers";

class DeviceManagerList extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            devices: [],
            page: 0,
            size: 12,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false,
            formVisible: false
        };
    }

    showDrawer = () => {
        this.setState({
            formVisible: true,
        });
    };

    onClose = () => {
        this.setState({
            formVisible: false,
        });
    };

    loadDeviceList = (page = 0, size = DEVICE_LIST_SIZE) => {
        this.setState({
            isLoading: true
        });

        getAllDevices(page, size)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        devices: response.content,
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
                description: error.message || "Get all devices failed.",
            });
        });
    };

    handleDelete = (id) => {
        deleteDevice(id)
            .then(response => {
                if (this._isMounted) {
                    const devices = [...this.state.devices];
                    this.setState({devices: devices.filter(item => item.id !== id)});
                }
            }).catch(error => {
            notification["error"]({
                message: 'NCT App',
                description: error.message || "Delete device failed.",
            });
        });
    };

    onNewDevice = () => {
        this.loadDeviceList(this.state.totalElements % DEVICE_LIST_SIZE === 0 ? this.state.totalPages : this.state.totalPages - 1);
        this.onClose();
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

    render() {
        const {devices} = this.state;
        const AntdWrappedNewDeviceForm = Form.create()(NewDeviceForm);

        const columns = [{
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 10,
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
        }, {
            title: 'Action',
            key: 'action',
            width: 70,
            render: (text, record) =>
                this.state.devices.length >= 1 ? (
                    <Popconfirm title="Sure to delete?" onConfirm={() => this.handleDelete(record.id)}>
                        <Button type="link">Delete</Button>
                    </Popconfirm>
                ) : null,
        }];

        return (
            <div>
                <h2 className="header">List of Devices</h2>
                <Button className="button" type="primary" htmlType="button" onClick={this.showDrawer}>
                    <Icon type="plus"/> Add Device</Button>
                <ConfigProvider renderEmpty={() => (
                    <Empty description=" No Device Found.">
                        <Button type="primary" htmlType="button" onClick={this.showDrawer}>
                            <Icon type="plus"/> Add Device</Button>
                    </Empty>
                )}>
                    <Table {...this.state} rowKey={record => record.id} columns={columns}
                           dataSource={devices} size="default"
                           pagination={{
                               onChange: (page) => this.handleLoadMore(page),
                               total: this.state.totalElements, showTotal: (total) => `Total ${total} users`,
                               pageSize: this.state.size, current: this.state.page
                           }}/>
                </ConfigProvider>
                <Drawer
                    title={<h3>Create a new device</h3>}
                    width={720}
                    onClose={this.onClose}
                    visible={this.state.formVisible}
                >
                    <AntdWrappedNewDeviceForm onNewDevice={this.onNewDevice}/>
                </Drawer>
            </div>
        );
    }
}

const sensorOptions = [1, 2, 3, 4, 5];
const actuatorOptions = [1, 2, 3, 4, 5, 6, 7, 8];

class NewDeviceForm extends Component {
    state = {
        sensor: {
            indeterminate: false,
            checkAll: true
        },
        actuator: {
            indeterminate: false,
            checkAll: true
        },
    };

    onSensorChange = checkedList => {
        this.setState({
            sensor: {
                indeterminate: !!checkedList.length && checkedList.length < sensorOptions.length,
                checkAll: checkedList.length === sensorOptions.length
            }
        });
    };

    onCheckAllSensorChange = e => {
        this.setState({
            sensor: {
                indeterminate: false,
                checkAll: e.target.checked
            }
        });
        this.props.form.setFieldsValue({
            sensors: e.target.checked ? sensorOptions : [1, 4]
        });
    };

    onActuatorChange = checkedList => {
        this.setState({
            actuator: {
                indeterminate: !!checkedList.length && checkedList.length < actuatorOptions.length,
                checkAll: checkedList.length === actuatorOptions.length
            }
        });
    };

    onCheckAllActuatorChange = e => {
        this.setState({
            actuator: {
                indeterminate: false,
                checkAll: e.target.checked
            }
        });
        this.props.form.setFieldsValue({
            actuators: e.target.checked ? actuatorOptions : []
        });
    };

    handleSubmit = (event) => {
        event.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                const createDeviceReq = Object.assign({}, values);
                createDevice(createDeviceReq)
                    .then(response => {
                        notification.success({
                            message: 'NCT App',
                            description: "Successful create device " + response.id || 'Successfully! Create new device done!'
                        });
                        this.props.onNewDevice(response);
                    }).catch(error => {
                    notification.error({
                        message: 'NCT App',
                        description: error.message || 'Sorry! Something went wrong. Please try again!'
                    });
                });
            }
        });
    };

    render() {
        const {getFieldDecorator} = this.props.form;

        return (
            <div>
                <Form onSubmit={this.handleSubmit} layout="vertical">
                    <Row gutter={16}>
                        <Col span={24}>
                            <Form.Item label={<h5>Device sensors</h5>}>
                                <div style={{borderBottom: '1px solid #E9E9E9'}}>
                                    <Checkbox
                                        indeterminate={this.state.sensor.indeterminate}
                                        onChange={this.onCheckAllSensorChange}
                                        checked={this.state.sensor.checkAll}
                                    >
                                        Check all
                                    </Checkbox>
                                </div>
                                <br/>
                                {getFieldDecorator('sensors', {
                                    initialValue: sensorOptions,
                                })(
                                    <Checkbox.Group style={{width: '100%'}} onChange={this.onSensorChange}>
                                        <Row>
                                            <Col span={8}>
                                                <Checkbox disabled value={PH_SENSOR}>pH Sensor</Checkbox>
                                            </Col>
                                            <Col span={8}>
                                                <Checkbox value={TEMPERATURE_SENSOR}>Temperature Sensor</Checkbox>
                                            </Col>
                                            <Col span={8}>
                                                <Checkbox value={HUMIDITY_SENSOR}>Humidity Sensor</Checkbox>
                                            </Col>
                                            <Col span={8}>
                                                <Checkbox disabled value={EC_SENSOR}>EC Sensor</Checkbox>
                                            </Col>
                                            <Col span={8}>
                                                <Checkbox value={LIGHT_SENSOR}>Light Sensor</Checkbox>
                                            </Col>
                                        </Row>
                                    </Checkbox.Group>
                                )}
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={24}>
                            <Form.Item label={<h5>Device actuators</h5>}>
                                <div style={{borderBottom: '1px solid #E9E9E9'}}>
                                    <Checkbox
                                        indeterminate={this.state.actuator.indeterminate}
                                        onChange={this.onCheckAllActuatorChange}
                                        checked={this.state.actuator.checkAll}
                                    >
                                        Check all
                                    </Checkbox>
                                </div>
                                <br/>
                                {getFieldDecorator('actuators', {
                                    initialValue: actuatorOptions
                                })(
                                    <Checkbox.Group style={{width: '100%'}} onChange={this.onActuatorChange}>
                                        <Row>
                                            <Col span={6}>
                                                <Checkbox value={PUMP_A}>A-solution Pump</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={PUMP_B}>B-solution Pump</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={PUMP_UP}>Pump up</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={PUMP_WATER}>Water Pump</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={PUMP_PH_UP}>pH-up Pump</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={PUMP_PH_DOWN}>pH-down Pump</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={LED}>Led</Checkbox>
                                            </Col>
                                            <Col span={6}>
                                                <Checkbox value={FAN}>Fan</Checkbox>
                                            </Col>
                                        </Row>
                                    </Checkbox.Group>
                                )}
                            </Form.Item>
                        </Col>
                    </Row>
                    <div
                        style={{
                            position: 'absolute',
                            left: 0,
                            bottom: 0,
                            width: '100%',
                            borderTop: '1px solid #e9e9e9',
                            padding: '10px 16px',
                            background: '#fff',
                            textAlign: 'right',
                        }}
                    >
                        <Button onClick={this.onClose} style={{marginRight: 8}}>
                            Cancel
                        </Button>
                        <Button htmlType="submit" type="primary">
                            Create
                        </Button>
                    </div>
                </Form>
            </div>
        )
    };
}

export default DeviceManagerList;