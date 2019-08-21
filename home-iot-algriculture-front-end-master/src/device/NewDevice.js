import React, {Component} from 'react';
import {ACCESS_TOKEN} from '../constants';
import './NewDevice.css';
import {Button, Icon, Steps, message} from 'antd';

const {Step} = Steps;

class NewDevice extends Component {
    constructor(props) {
        super(props);
        this.state = {
            current: 0
        };
    }

    next() {
        const current = this.state.current + 1;
        switch (current) {
            case 0:

                break;
            default:
                break;
        }
        this.setState({current});
    }

    prev() {
        const current = this.state.current - 1;
        this.setState({current});
    }

    componentDidUpdate() {
        if (this.ifr) {
            this.ifr.onload = () => {
                const token = localStorage.getItem(ACCESS_TOKEN);
                if (token) this.ifr.contentWindow.postMessage(token, "*");
            }
        }
    }

    handleFinish = () => {
        message.success('Add new device successfully!');
        this.props.history.push('/device');
    };

    render() {
        const {current} = this.state;
        const steps = [{
            title: 'Device Connect',
            content:
                <div>
                    <p>Open your wifi setting and connect to this wifi:<br/>
                        SSID: ESP32<br/>
                        Password: esp32pwd<br/>
                        Click "Next" when you've done.</p>
                </div>,
            description: "Connect to ESP Wifi",
            icon: <Icon type="link"/>,
        }, {
            title: 'Wifi Setup',
            content:
                <iframe src="http://192.168.1.1" sandbox="allow-scripts allow-same-origin"
                        ref={(f) => this.ifr = f} width="720" title="Wifi manager"
                        height="480">This browsers does not support this feature.
                </iframe>,
            description: 'Connect internet',
            icon: <Icon type="wifi"/>,
        }, {
            title: 'Finish',
            content: <p>Reconnect to your wifi. <br/>Press button on your ESP to turn Wifi AP off.</p>,
            description: 'Your device is ready',
            icon: <Icon type="check-circle"/>,
        }];

        return (
            <div className="new-device-container">
                <h1 className="page-title">Register New Device</h1>
                <div>
                    <Steps current={current}>
                        {steps.map(item => <Step key={item.title} title={item.title} icon={item.icon}
                                                 description={item.description}/>)}
                    </Steps>
                    <div className="steps-content">{steps[current].content}</div>
                    <div className="steps-action">
                        {
                            current < steps.length - 1
                            && <Button type="primary" onClick={() => this.next()} htmlType="button">Next</Button>
                        }
                        {
                            current === steps.length - 1
                            && <Button type="primary" onClick={this.handleFinish} htmlType="submit">Done</Button>
                        }
                        {
                            current > 0
                            && (
                                <Button style={{marginLeft: 8}} onClick={() => this.prev()} htmlType="button">
                                    Previous
                                </Button>
                            )
                        }
                    </div>
                </div>
            </div>
        );
    }
}

export default NewDevice;