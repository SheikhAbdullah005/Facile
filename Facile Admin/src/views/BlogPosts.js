/* eslint jsx-a11y/anchor-is-valid: 0 */

import React from "react";
import {
  Container,
  Row,
  ButtonGroup,
  Card,
  CardBody,
  Form,
  Alert,
  FormInput,
  Button,
  CardFooter,

} from "shards-react";

import axios from 'axios';
import PageTitle from "../components/common/PageTitle";
import { Redirect } from 'react-router-dom';

class BlogPosts extends React.Component {
  state = {
    service: null,
    service1: null,
    selectedService:'',
    serviceID:'',
    alert_message: ''
  } 
  componentDidMount(){
    axios.get(`http://localhost:3005/api/fetch_dept_services`)
      .then(res => {
        const service = res.data;
        this.setState({ service });
        console.log('check1',service);
      }).catch(err=>{
        console.log('always add catch after then',err)
      })
  }
  changeService = (selectedService) =>{
    this.setState({selectedService})
  }
  renderServiceData = () => {
    return this.state.service.map(elem=>{
      return <div
            onChange={() => {
              this.changeService(elem.service_id);
              this.changeService(elem.service_name);
              this.changeService(elem.service_des);
              this.changeService(elem.service_price);
              this.changeService(elem.department_name);
            }}
          >
          <Container fluid className="main-content-container px-2 pb-2">
            <Card small className="card-post h-100">
            <CardBody>
              <h4 className="card-title">
                  {elem.service_name}
              </h4>
              <h6 className="card-title">
                {elem.department_name}
              </h6>
              <p className="card-text">
                {elem.service_des}
              </p>
              <p className="card-text">
                {elem.service_price}
              </p>
            </CardBody>
            <CardFooter className="text-muted border-top py-3">
            <div className="blog-comments__actions">
              <ButtonGroup size="sm">
                <Button theme="white" onClick={()=>this.updateService(elem.service_id)}>
                    <span className="text-light">
                      <i className="material-icons">more_vert</i>
                    </span>{" "}
                    Edit
                  </Button>
                <Button theme="white" onClick={()=>this.deleteService(elem.service_id)}>
                  <span className="text-danger">
                    <i className="material-icons">clear</i>
                  </span>{" "}
                  Delete
                </Button>
              </ButtonGroup>
            </div>
            </CardFooter>
          </Card>
          </Container>
        </div>
    })
  }
  updateService(serviceId)
  {
    console.log("Clicked Service ID: ", serviceId) 
    this.serviceID = serviceId;
    axios.put(`http://localhost:3005/api/fetch_single_service/${serviceId}`)
      .then(res => {
        const service1 = res.data;
        this.setState({ service1 });
        console.log('check5',service1);
      }).catch(err=>{
        console.log('failed',err)
      })
  }
  renderUpdateServiceData = () => {
    return this.state.service1.map(elem=>{
      return <div>
          <Container fluid className="main-content-container px-2 pb-2">
          <Card Large className="mb-3">
              <CardBody>
                <Form className="add-new-post" onSubmit={this.submitChangeHandler}>
                <label>Service Id</label>
                  <FormInput 
                      disabled
                      size="lg"
                      className="mb-3"
                      value={elem.service_id}
                    />
                  <label>Service Name</label>
                  <FormInput 
                    size="lg"
                    className="mb-3"
                    placeholder="Service Title"
                    name="service_name"
                    value={elem.service_name}
                    onChange={this.changeHandler}
                  />
                 <label>Service Description</label>
                  <FormInput 
                    size="lg" 
                    className="mb-3" 
                    placeholder="Service Description" 
                    name="service_des"
                    value={elem.service_des}
                    onChange={this.changeHandler}
                  />
                  <label>Service Price</label>
                  <FormInput 
                    size="lg" 
                    className="mb-3" 
                    placeholder="Service Initial Price (PKR)" 
                    name="service_price"
                    value={elem.service_price}
                    onChange={this.changeHandler}
                  />
                  <Button type = "submit" theme="accent">Update Service</Button>
                </Form>
              </CardBody>
            </Card>
            </Container>
        </div>
    })
  }
  deleteService(serviceId)
  {
    axios.put(`http://localhost:3005/api/delete_service/${serviceId}`)
      .then(res => {
        const { service }  = this.state;
        this.setState({ 
          service: service.filter(service1 => service1.service_id !== serviceId)
         });
        console.log('check1',service);
      }).catch(err=>{
        console.log('always add catch after then',err)
      })
  }
  changeHandler = e => {
    let service = this.state.service1[0];
    service[e.target.name] = e.target.value;
    service  = [service];
    this.setState({ service1 : service });
  }
  submitChangeHandler = e => {
    e.preventDefault()
    const vars = this.state.service1[0].service_id;
    console.log(vars)
    axios.put(`http://localhost:3005/api/update_service/${vars}`,this.state.service1[0]) 
      .then(res => {
        const service1 = res.data;
        this.setState({alert_message: "Success"})
        console.log('check10',this.state.alertmessage);
        console.log('check6',service1);
      }).catch(err=>{
        this.setState({alert_message: "Error"})
        console.log('WTF',err)

      })
  }
  render() {
    if(this.state.alert_message==="Success"){
      return <Redirect to="/add-new-post" />
    }
    return (
      <Container fluid className="main-content-container px-4">
        {/* Page Header */}
        <Row noGutters className="page-header py-4">
          <PageTitle sm="4" title="All Services" subtitle="Departments" className="text-sm-left" />
        </Row>
        {/* First Row of Posts */}
        <Row lg="3" md="6" sm="12" className="mb-4">
            { this.state.service ?  this.renderServiceData() : null }
        </Row>
        <Row>
          {this.state.alert_message==="Success"?
          <Alert theme="success">
            Service Updated Successfully!
          </Alert> :null
          }
        </Row> 
        <Row md="12" className="mb-4">
            { this.state.service1 ?  this.renderUpdateServiceData() : null  }
        </Row>
      </Container>
    );
  }
}

export default BlogPosts;
