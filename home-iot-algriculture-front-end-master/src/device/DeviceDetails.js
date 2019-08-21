import React, {Component} from 'react';
import './DeviceDetails.css';
import {Button, Card, Col, Form, InputNumber, List, notification, Radio, Statistic} from "antd";
import {getActuatorSwitch, getCurrentStatus, getCurrentValue, getUnits} from "../util/Helpers";
import {getDeviceDetails, sendCommand} from "../util/APIUtils";
import {Client} from '@stomp/stompjs';
import {BROKER_URL} from "../constants";

class DeviceDetails extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            id: 0,
            alive: false,
            sensors: [],
            actuators: [],
            createdAt: null,
            updatedAt: null,
        };
    }

    componentDidMount() {
        this._isMounted = true;
        this.loadDeviceDetails(this.props.match.params.deviceId);

    }

    componentWillUnmount() {
        this._isMounted = false;
        if (this.client) this.client.deactivate();
    }

    componentDidUpdate() {
        if (this.state.id && this.state.sensors && !this.client) this.connectSocket();
    }

    connectSocket = () => {
        this.client = new Client();
        this.client.configure({
            brokerURL: BROKER_URL,
            onConnect: () => {
                this.client.subscribe('/topic/sensor/' + this.state.id, message => {
                    const currentValues = JSON.parse(message.body);
                    const newSensors = this.state.sensors;
                    newSensors.forEach(item => item.currentValue = getCurrentValue(item.type, currentValues));
                    if (this._isMounted) this.setState({
                        sensors: newSensors
                    })
                });
                this.client.subscribe('/topic/actuator/' + this.state.id, message => {
                    const currentStatus = JSON.parse(message.body);
                    const newActuators = this.state.actuators;
                    newActuators.forEach(item => item.status = getCurrentStatus(item.type, currentStatus));
                    if (this._isMounted) this.setState({
                        alive: true,
                        actuators: newActuators
                    })
                });
            },
            // // Helps during debugging, remove in production
            // debug: (str) => {
            //     console.log(new Date(), str);
            // }
        });

        this.client.activate();
    };

    loadDeviceDetails = (deviceId) => {
        this.setState({
            isLoading: true
        });

        getDeviceDetails(deviceId)
            .then(response => {
                    if (this._isMounted) this.setState(response);
                }
            ).catch(error => {
            this.setState({
                isLoading: false
            });
            notification["error"]({
                message: 'NCT App',
                description: error.message || "Get device detail failed.",
            });
        });
    };

    handleSubmit = (event, actuatorType) => {
        event.preventDefault();
        const {actuators} = this.state;
        const {value} = event.target;
        const actuator = this.state.actuators.find((ele) => {
            return ele.type === actuatorType
        });
        console.log(actuator);
        const commandRequest = {
            param: actuator.param || 60,
            action: value,
            actuatorName: getActuatorSwitch(actuatorType),
            deviceId: this.state.id
        };
        console.log(commandRequest);
        sendCommand(commandRequest)
            .then(response => {
                notification.success({
                    message: 'NCT App',
                    description: response.message
                });
                this.setState({
                    actuator: actuators.map((ele) => {
                        if (ele.type === actuatorType) ele.status = value;
                        return ele;
                    })
                })
            }).catch(error => {
            notification.error({
                message: 'NCT App',
                description: error.message || 'Sorry! Something went wrong. Please try again!'
            });
        });
    };

    handleParamChange = (event, actuatorType) => {
        const {actuators} = this.state;
        this.setState({
            actuators: actuators.map((ele) => {
                if (ele.type === actuatorType) {
                    ele.param = event;
                }
                return ele;
            })
        });
    };

    render() {
        const {sensors, actuators, alive} = this.state;
        const {deviceId} = this.props.match.params;
        const crop = this.props.location.state;

        return (
            <div>
                <h1 className="header">ESP32_ID_{deviceId}</h1>
                {crop ? <Button type="link" className="header-link"
                                onClick={() => this.props.history.push('/crop/' + crop.id)}>Crop {crop.name}</Button> : null}
                <h3>Sensor:</h3>
                <List className="list-container"
                      grid={{gutter: 16, xs: 1, sm: 2, md: 3, lg: 3,}}
                      dataSource={sensors}
                      renderItem={item => (
                          <List.Item>
                              <Card>
                                  <Statistic
                                      title={item.name}
                                      value={item.currentValue}
                                      valueStyle={{color: '#008888'}}
                                      precision={2}
                                      suffix={getUnits(item.type)}
                                  />
                              </Card>
                          </List.Item>
                      )}
                />
                <h3>Actuator:</h3>
                <List className="list-container"
                      grid={{gutter: 16, xs: 1, sm: 2, md: 3, lg: 3,}}
                      dataSource={actuators}
                      renderItem={item => (
                          <List.Item>
                              <Card title={item.name}>
                                  <p>ACTUATOR_ID_{item.id}</p>
                                  <Form>
                                      <Form.Item style={{marginBottom: 0}}>
                                          <Col sm={8}>Control time:</Col>
                                          <Col sm={16}>
                                              <InputNumber name="param" step={10} formatter={value => `${value} s`}
                                                           disabled={!alive} defaultValue={60}
                                                           onChange={(event) => this.handleParamChange(event, item.type)}/>
                                          </Col>
                                      </Form.Item>
                                      <Form.Item style={{marginBottom: 0}}>
                                          <Col sm={8}>Control:</Col>
                                          <Col sm={16}>
                                              <Radio.Group name="action"
                                                           onChange={(event) => this.handleSubmit(event, item.type)}
                                                           disabled={!alive} value={item.status}>
                                                  <Radio.Button value="ON">ON</Radio.Button>
                                                  <Radio.Button value="OFF">OFF</Radio.Button>
                                              </Radio.Group>
                                          </Col>
                                      </Form.Item>
                                  </Form>
                              </Card>
                          </List.Item>
                      )}
                />
            </div>
        );
    }
}

export default DeviceDetails;