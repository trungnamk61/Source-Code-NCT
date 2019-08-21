import React, {Component} from 'react';
import {createCrop, getPlantList} from '../util/APIUtils';
import {CROP_NAME_MAX_LENGTH} from '../constants';
import './NewCrop.css';
import {Form, Input, Button, Select, Col, notification} from 'antd';
import DeviceTable from "../device/DeviceTable";

const Option = Select.Option;
const FormItem = Form.Item;

class NewCrop extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            name: {
                text: ''
            },
            deviceId: 0,
            plant: {},
            plants: [],
            isLoading: false
        };
        this.handlePlantChange = this.handlePlantChange.bind(this);
    }

    handleSubmit = (event) => {
        event.preventDefault();
        const cropData = {
            name: this.state.name.text,
            deviceId: this.state.deviceId,
            plantId: this.state.plant.id
        };

        createCrop(cropData)
            .then(response => {
                notification.success({
                    message: 'NCT App',
                    description: response.message || 'Create Crop successfully!'
                });
                this.props.history.push("/crop");
            }).catch(error => {
            if (error.status === 401) {
                this.props.handleLogout('/login', 'error', 'You have been logged out. Please login create crop.');
            } else {
                notification.error({
                    message: 'NCT App',
                    description: error.message || 'Sorry! Something went wrong. Please try again!'
                });
            }
        });
    };

    validateName = (questionText) => {
        if (questionText.length === 0) {
            return {
                validateStatus: 'error',
                errorMsg: "Please enter your Crop's name!"
            }
        } else if (questionText.length > CROP_NAME_MAX_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Crop's name is too long (Maximum ${CROP_NAME_MAX_LENGTH} characters allowed)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null
            }
        }
    };

    handleNameChange = (event) => {
        const value = event.target.value;
        this.setState({
            name: {
                text: value,
                ...this.validateName(value)
            }
        });
    };

    isFormInvalid = () => {
        if (this.state.name.validateStatus !== 'success') {
            return true;
        }

        if (this.state.deviceId === 0) {
            return true;
        }
    };

    componentDidMount() {
        this._isMounted = true;
        this.loadPlantList();
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    loadPlantList = () => {
        this.setState({
            isLoading: true
        });

        getPlantList()
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        plants: response,
                        plant: response[0],
                        isLoading: false
                    })
                }
            }).catch(error => {
            this.setState({
                isLoading: false
            });
            notification["error"]({
                message: 'NCT App',
                description: error.message || "Get Plant list failed.",
            });
        });
    };

    handlePlantChange = (event) => {
        this.setState({plant: this.state.plants[event - 1]})
    };

    onSelectDeviceChange = (selectedRowKeys) => {
        this.setState({deviceId: selectedRowKeys[0]});
    };

    render() {
        const {plant} = this.state;

        return (
            <div className="new-crop-container">
                <h1 className="page-title">Create New Crop</h1>
                <div>
                    <Form onSubmit={this.handleSubmit} className="create-crop-form">

                        <FormItem validateStatus={this.state.name.validateStatus}
                                  help={this.state.name.errorMsg} className="crop-form-row">
                            <Input
                                placeholder="Enter crop name"
                                size="large"
                                value={this.state.name.text}
                                onChange={(event) => this.handleNameChange(event, this.state.name)}/>
                        </FormItem>

                        <FormItem className="crop-form-row">
                            <Col xs={24} sm={4}>
                                Choose your plant:
                            </Col>
                            <Col xs={24} sm={20}>
                                <span style={{marginRight: '18px'}}>
                                    <Select
                                        name="plant"
                                        defaultValue="1"
                                        value={plant.id + ""}
                                        onChange={(value) => this.handlePlantChange(value)}
                                        style={{width: 200}}>
                                        {
                                            this.state.plants.map(plant =>
                                                <Option key={plant.id}>{plant.name}</Option>
                                            )
                                        }
                                    </Select>
                                </span>
                            </Col>
                        </FormItem>

                        <FormItem className="crop-form-row">
                            <Col xs={24} sm={4}>
                                Range of plant eC:
                            </Col>
                            <Col xs={24} sm={6}>
                                <span style={{marginRight: '18px'}}>
                            <Input placeholder="min eC" size="large" value={plant.minEC} disabled={true}
                                   style={{width: 50}}/>~
                            <Input placeholder="max eC" size="large" value={plant.maxEC} disabled={true}
                                   style={{width: 50}}/> mS / cm
                                </span>
                            </Col>

                            <Col xs={24} sm={2}>
                                Growing day:
                            </Col>
                            <Col xs={24} sm={10}>
                                Early:
                                <Input placeholder="earlyDay" size="large" value={plant.earlyDay}
                                       disabled={true}
                                       style={{width: 50}}/>
                                Mid:
                                <Input placeholder="midDay" size="large" value={plant.midDay} disabled={true}
                                       style={{width: 50}}/>
                                Late:
                                <Input placeholder="lateDay" size="large" value={plant.lateDay}
                                       disabled={true}
                                       style={{width: 50}}/>
                            </Col>
                            <Col xs={24} sm={2}>
                                Total: {plant.earlyDay + plant.midDay + plant.lateDay} days
                            </Col>
                            <Col xs={24} sm={4}>
                                Range of plant pH:
                            </Col>
                            <Col xs={24} sm={6}>
                                <span>
                            <Input placeholder="min pH" size="large" value={plant.minPH} disabled={true}
                                   style={{width: 50}}/>~
                            <Input placeholder="max pH" size="large" value={plant.maxPH} disabled={true}
                                   style={{width: 50}}/>
                                </span>
                            </Col>
                        </FormItem>

                        <FormItem className="crop-form-row">
                            <Col>
                                Choose a device to collect:
                            </Col>
                            <Col>
                                <DeviceTable selectable={true} onSelectDeviceChange={this.onSelectDeviceChange} {...this.props}/>
                            </Col>
                        </FormItem>

                        <FormItem className="crop-form-row">
                            <Button type="primary"
                                    htmlType="submit"
                                    size="large"
                                    disabled={this.isFormInvalid()}
                                    className="create-crop-form-button">Create New Crop</Button>
                        </FormItem>
                    </Form>
                </div>
            </div>
        );
    }
}

export default NewCrop;