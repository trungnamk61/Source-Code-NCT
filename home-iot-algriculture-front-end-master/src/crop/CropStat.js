import React, {Component} from 'react';
import {getCropDetails, getSensorData, stopCrop} from '../util/APIUtils';
import {Button, Col, Icon, Row, message, Card} from 'antd';
import {BROKER_URL, EC_SENSOR, HUMIDITY_SENSOR, LIGHT_SENSOR, PH_SENSOR, TEMPERATURE_SENSOR} from '../constants';
import './CropStat.css';
import LoadingIndicator from "../common/LoadingIndicator";
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ReferenceLine,
    ResponsiveContainer
} from 'recharts';
import {dayNo, formatDateTime, getCurrentValue, getImgUrl, totalDate} from "../util/Helpers";
import moment from "moment";
import {Client} from "@stomp/stompjs";

class CropList extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            id: 0,
            name: "",
            startTime: null,
            endTime: null,
            device: [],
            plant: [],
            sensors: [],
            sensorData: null,
            isLoading: false,
        };
    }

    loadCropDetails = (cropId) => {
        this.setState({
            isLoading: true
        });

        getCropDetails(cropId)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        id: response.id,
                        name: response.name,
                        startTime: response.startTime,
                        endTime: response.endTime,
                        device: response.device,
                        plant: response.plant,
                        sensors: response.sensors,
                        isLoading: false
                    });
                }
            }).catch(error => {
            this.setState({
                isLoading: false
            });
            message.error(error.message || "Load Crop Details false.");
        });

    };

    parseData = (response) => {
        return {
            sensorData: response.map(item => {
                return {
                    id: item.id,
                    name: item.name,
                    type: item.type,
                    data: item.data.map(item => {
                        item.timestamp = new Date(item.timestamp).getTime();
                        return item;
                    })
                }
            })
        }
    };

    loadSensorData = (cropId) => {
        getSensorData(cropId)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        ...this.parseData(response)
                    });
                }
            }).catch(error => {
            message.error(error.message);
        });
    };

    connectSocket = () => {
        const {device} = this.state;
        this.client = new Client();
        this.client.configure({
            brokerURL: BROKER_URL,
            onConnect: () => {
                this.client.subscribe('/topic/sensor/' + this.state.device.id, receiveMessage => {
                    const currentValues = JSON.parse(receiveMessage.body);
                    const newSensorData = this.state.sensorData;
                    newSensorData.forEach(item => {
                        item.data.push({
                            value: getCurrentValue(item.type, currentValues),
                            timestamp: new Date().getTime()
                        });
                    });
                    if (this._isMounted) this.setState({
                        sensorData: newSensorData
                    })
                });
                this.client.subscribe('/topic/actuator/' + device.id, () => {
                    device.alive = true;
                    if (this._isMounted) this.setState({
                        device: device
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

    componentWillUnmount() {
        this._isMounted = false;
        if (this.client) this.client.deactivate();
    }

    componentDidMount() {
        this._isMounted = true;
        this.loadCropDetails(this.props.match.params.cropId);
        this.loadSensorData(this.props.match.params.cropId);
    }

    componentDidUpdate() {
        if (this.state.device && this.state.sensorData && !this.client) this.connectSocket();
    }

    getData = (sensor) => {
        const {sensorData} = this.state;
        const data = sensorData ? sensorData.find((ele) => {
            return ele.type === sensor;
        }) : null;

        return data ? data.data.slice() : null;
    };

    handleStopCrop = () => {
        stopCrop(this.state.id)
            .then(response => {
                message.success(response.message);
                window.location.reload();
            }).catch(error => {
            message.error(error.message)
        });
    };

    render() {
        const {isLoading, name, device, startTime, endTime, plant} = this.state;
        const {currentUser} = this.props;

        return (
            isLoading ? <LoadingIndicator/> :
                <div>
                    <Row>
                        <Col sm={21}>
                            <h1 className="header">{currentUser ? currentUser.name : null}'s Crop: {name}</h1>
                        </Col>
                        <Col sm={3}>
                            {endTime ? null :
                                <Button type="danger" htmlType="button" className="button"
                                        onClick={this.handleStopCrop}>
                                    Stop Crop</Button>}
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col xs={24} sm={12}>
                            <h3>Device</h3>
                            <Card
                                className="numberCard"
                                bordered={false}
                                bodyStyle={{padding: 10}}
                                hoverable={true}
                                onClick={() =>
                                    this.props.history.push('/device/' + device.id, {...this.state})}
                            >
                                <Icon className="iconWarp" style={{"color": device.alive ? "#64ea91" : "#ff1818"}}
                                      type="box-plot"/>
                                <div className="content">
                                    <p className="title">{"Device ESP32_ID_" + device.id}</p>
                                    <p className="number">
                                        {device.alive ?
                                            <span style={{"color": "#64ea91"}}>
                                                Collecting <Icon type="loading"/></span> :
                                            <span style={{"color": "#ff1818"}}>
                                                Disconnected <Icon type="disconnect"/></span>}
                                    </p>
                                </div>
                            </Card>
                            <h3 style={{"display": "inline-block"}}>Time</h3><h5 style={{
                            "display": "inline-block",
                            float: "right"
                        }}>{endTime ? "Total: " + totalDate(startTime, endTime) + "days" : dayNo(startTime)}</h5>
                            <h5>
                                Start: {formatDateTime(startTime)}
                            </h5>{endTime ?
                            <h5>
                                End: {formatDateTime(endTime)}
                            </h5>
                            : ""}
                        </Col>
                        <Col xs={24} sm={12}>
                            <figure className="figure">
                                <img alt="plant-img" src={getImgUrl(plant.id)}
                                     style={{"width": "100%", height: 300}}/>
                                <figcaption className="figcaption">
                                    <h2 className="hover-text">Plant type: {plant.name}</h2>
                                </figcaption>
                            </figure>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col xs={24} lg={12}>
                            <h3>eC</h3>
                            <ResponsiveContainer width='100%' height={300}>
                                <LineChart data={this.getData(EC_SENSOR)}
                                           cx="50%"
                                           cy="50%"
                                           outerRadius="80%">
                                    <XAxis dataKey="timestamp"
                                           tickFormatter={unixTime => moment(unixTime).format('HH:mm DD/MM')}
                                           domain={['dataMin', 'dataMax']}
                                           name='Time' type='number'/>
                                    <YAxis dataKey='value' name='Value'/>
                                    <CartesianGrid strokeDasharray="3 3"/>
                                    <Tooltip
                                        labelFormatter={unixTime => moment(unixTime).format('MMMM Do YYYY, hh:mm:ss a')}/>
                                    <Legend/>
                                    <Line type="monotone" dataKey="value" label="eC" dot={false} stroke="#8884d8"/>
                                    <ReferenceLine y={plant.maxEC} label="Max" stroke="red" strokeDasharray="3 3"/>
                                    <ReferenceLine y={plant.minEC} label="Min" stroke="red" strokeDasharray="3 3"/>
                                </LineChart>
                            </ResponsiveContainer>
                        </Col>
                        <Col xs={24} lg={12}>
                            <h3>pH</h3>
                            <ResponsiveContainer width='100%' height={300}>
                                <LineChart data={this.getData(PH_SENSOR)}
                                           cx="50%"
                                           cy="50%"
                                           outerRadius="80%">
                                    <XAxis dataKey="timestamp"
                                           tickFormatter={unixTime => moment(unixTime).format('HH:mm DD/MM')}
                                           domain={['dataMin', 'dataMax']} name='Time' type='number'/>
                                    <YAxis/>
                                    <CartesianGrid strokeDasharray="3 3"/>
                                    <Tooltip
                                        labelFormatter={unixTime => moment(unixTime).format('MMMM Do YYYY, hh:mm:ss a')}/>
                                    <Legend/>
                                    <Line type="monotone" dataKey="value" dot={false} stroke="#82ca9d"/>
                                    <ReferenceLine y={plant.maxPH} label="Max" stroke="red" strokeDasharray="3 3"/>
                                    <ReferenceLine y={plant.minPH} label="Min" stroke="red" strokeDasharray="3 3"/>
                                </LineChart>
                            </ResponsiveContainer>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col xs={24} sm={8} lg={8}>
                            <h3>Temperature</h3>
                            <ResponsiveContainer width='100%' height={300}>
                                <LineChart data={this.getData(TEMPERATURE_SENSOR)}
                                           cx="50%"
                                           cy="50%"
                                           outerRadius="80%">
                                    <XAxis dataKey="timestamp"
                                           tickFormatter={unixTime => moment(unixTime).format('DD/MM')}
                                           domain={['dataMin', 'dataMax']} name='Time' type='number'/>
                                    <YAxis/>
                                    <CartesianGrid strokeDasharray="3 3"/>
                                    <Tooltip
                                        labelFormatter={unixTime => moment(unixTime).format('MMMM Do YYYY, hh:mm:ss a')}/>
                                    <Legend/>
                                    <Line type="monotone" dataKey="value" dot={false} stroke="#8884d8"/>
                                </LineChart>
                            </ResponsiveContainer>
                        </Col>
                        <Col xs={24} sm={8} lg={8}>
                            <h3>Humidity</h3>
                            <ResponsiveContainer width='100%' height={300}>
                                <LineChart data={this.getData(HUMIDITY_SENSOR)}
                                           cx="50%"
                                           cy="50%"
                                           outerRadius="80%">
                                    <XAxis dataKey="timestamp"
                                           tickFormatter={unixTime => moment(unixTime).format('DD/MM')}
                                           domain={['dataMin', 'dataMax']} name='Time' type='number'/>
                                    <YAxis/>
                                    <CartesianGrid strokeDasharray="3 3"/>
                                    <Tooltip
                                        labelFormatter={unixTime => moment(unixTime).format('MMMM Do YYYY, hh:mm:ss a')}/>
                                    <Legend/>
                                    <Line type="monotone" dataKey="value" dot={false} stroke="#82ca9d"/>
                                </LineChart>
                            </ResponsiveContainer>
                        </Col>
                        <Col xs={24} sm={8} lg={8}>
                            <h3>Light</h3>
                            <ResponsiveContainer width='100%' height={300}>
                                <LineChart data={this.getData(LIGHT_SENSOR)}
                                           cx="50%"
                                           cy="50%"
                                           outerRadius="80%">
                                    <XAxis dataKey="timestamp"
                                           tickFormatter={unixTime => moment(unixTime).format('DD/MM')}
                                           domain={['dataMin', 'dataMax']} name='Time' type='number'/>
                                    <YAxis/>
                                    <CartesianGrid strokeDasharray="3 3"/>
                                    <Tooltip
                                        labelFormatter={unixTime => moment(unixTime).format('MMMM Do YYYY, hh:mm:ss a')}/>
                                    <Legend/>
                                    <Line type="monotone" dataKey="value" dot={false} stroke="#82ca9d"/>
                                </LineChart>
                            </ResponsiveContainer>
                        </Col>
                    </Row>
                </div>
        );
    }
}

export default CropList;