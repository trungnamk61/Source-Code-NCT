import React, {Component} from 'react';
import {deleteCrop, getAllCrops} from '../util/APIUtils';
import {Button, Card, ConfigProvider, Empty, Icon, List, message, notification, Popconfirm} from 'antd';
import {CROP_LIST_SIZE} from '../constants';
import './CropList.css';
import LoadingIndicator from "../common/LoadingIndicator";
import {formatDateTime, getImgUrl} from "../util/Helpers";

const {Meta} = Card;

class CropList extends Component {
    _isMounted = false;

    constructor(props) {
        super(props);
        this.state = {
            crops: [],
            page: 0,
            size: 15,
            totalElements: 0,
            totalPages: 0,
            last: true,
            isLoading: false
        };
    }

    loadCropList = (page = 0, size = CROP_LIST_SIZE) => {
        this.setState({
            isLoading: true
        });

        getAllCrops(page, size)
            .then(response => {
                if (this._isMounted) {
                    this.setState({
                        crops: response.content,
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
                description: error.message || "Get all Crop failed.",
            });
        });

    };

    componentWillUnmount() {
        this._isMounted = false;
    }

    componentDidMount() {
        this._isMounted = true;
        this.loadCropList();
    }

    handleLoadMore = (page) => {
        this.loadCropList(page - 1);
    };

    handleDeleteCrop = (cropId) => {
        if (this.state.crops.find(el => el.id === cropId).endTime === null) {
            message.error("It's planting crop. Stop collecting and try again.");
            return;
        }

        deleteCrop(cropId).then(response => {
            message.success(response.message);
            if (this._isMounted) {
                this.setState(prevState => ({
                    crops: prevState.crops.filter(el => el.id !== cropId)
                }));
            }
        }).catch(error => {
            message.error(error.message);
        });
    };

    render() {
        const {isLoading, crops} = this.state;

        return (
            isLoading ? <LoadingIndicator/> :
                <div>
                    <h2 className="header">{this.props.currentUser ? this.props.currentUser.name : null}'s Crop</h2>
                    <Button type="primary" htmlType="button" className="button" onClick={()=>this.props.history.push("/crop/new")}>+ New Crop</Button>
                    <ConfigProvider renderEmpty={() => (
                        <Empty description="You haven't started any crop yet.">
                            <Button type="primary" htmlType="button" onClick={()=>this.props.history.push("/crop/new")}>+ Add New Crop</Button>
                        </Empty>
                    )}>
                        <List className="list-container"
                              grid={{gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 3, xxl: 3,}}
                              dataSource={crops}
                              renderItem={item => (
                                  <List.Item>
                                      <Card title={<div style={{width: "100%"}}>{item.name} {item.done ?
                                          <span style={{float: "right", color: "#00ff00"}}>
                                              <Icon type="check-circle"/> {item.endTime ? "Harvested" : "Done"}
                                          </span> : null}</div>}
                                            cover={<img alt="plant-img"
                                                        src={getImgUrl(item.plantId)}/>}
                                            actions={[
                                                <Icon type="box-plot" style={{fontSize: '18px'}}
                                                      onClick={() => this.props.history.push('/device/' + item.deviceId, {...item})}/>,
                                                <Icon type="bar-chart" style={{fontSize: '18px'}}
                                                      onClick={() => this.props.history.push('/crop/' + item.id)}/>,
                                                <Popconfirm title="Are you sure delete this crop?"
                                                            onConfirm={() => this.handleDeleteCrop(item.id)}>
                                                    <Icon type="delete" style={{color: "#FF0000", fontSize: '18px'}}/>
                                                </Popconfirm>
                                            ]}>
                                          <Meta
                                              title={item.plantTypeName}
                                              description=
                                                  {
                                                      <p className="crop-description">
                                                          Start time: {formatDateTime(item.startTime)} <br/>
                                                          {item.endTime ? " End time: " + formatDateTime(item.endTime) :
                                                              "Planting..."}
                                                      </p>
                                                  }
                                          /></Card>
                                  </List.Item>
                              )}
                        />
                    </ConfigProvider>
                </div>
        );
    }
}

export default CropList;