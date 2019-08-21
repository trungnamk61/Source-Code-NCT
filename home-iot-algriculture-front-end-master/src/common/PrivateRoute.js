import React from 'react';
import {
    Route,
    Redirect
} from "react-router-dom";
import {ACCESS_TOKEN, ROLE} from "../constants";


const PrivateRoute = ({component: Component, roles, ...rest}) => (
    <Route
        {...rest}
        render={props => {
            if (!localStorage.getItem(ACCESS_TOKEN)) {
                // not logged in so redirect to login page with the return url
                return <Redirect to={{pathname: '/login', state: {from: props.location}}}/>
            }
            
            // check if route is restricted by role
            if (roles && roles.indexOf(localStorage.getItem(ROLE)) === -1) {
                return <Redirect to={{pathname: '/', state: {from: props.location}}}/>
            }

            return <Component {...rest} {...props} />
        }}/>
);

export default PrivateRoute