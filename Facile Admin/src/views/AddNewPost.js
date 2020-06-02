import React from "react";
import { Container, Row, Col,Card, CardBody, Form, Button, FormInput, FormRadio, ListGroupItem, Alert } from "shards-react";
import PageTitle from "../components/common/PageTitle";
import { Redirect } from 'react-router-dom'
import axios from 'axios';

class AddNewDept extends React.Component {

  state = {
    department: [],
    selectedDepartment:'',
    department_id: '',
    service_name: '',
    service_des: '',
    service_price: '',
    message: '',
    alert_message: ''
  } 

  constructor(props) {
    super(props)
    const token = localStorage.getItem("token")

    let loggedIn = true
    if(token == null){
      loggedIn = false
    }
    this.state = {
      loggedIn
    }
  }

  componentDidMount(){
    axios.get(`http://localhost:3005/api/get_department`)
      .then(res => {
        const department = res.data;
        this.setState({ department });
        console.log('check1',department);
      }).catch(err=>{
        console.log('always add catch after then',err)
      })
  }
  changeDepartment = (selectedID, selectedDepartment) =>{
    this.setState({department_id: selectedID, department_name: selectedDepartment})
  }
  renderRadioButtons = () => {
    return this.state.department.map(elem=>{
      return <FormRadio
        checked={this.state.department_name === elem.department_name}
        onChange={() => {
          this.changeDepartment(elem.department_id,elem.department_name);
        }}
      >
        {elem.department_name}
      </FormRadio>
    })
  }
  changeHandler = e => {
    this.setState({ [e.target.name]: e.target.value })
  }
  submitHandler = e => {
    e.preventDefault()
    console.log(this.state)
    axios.post('http://localhost:3005/api/add_service', this.state)
      .then(res => {
        const department = res.data;
        this.message = res.data;
        this.setState({alert_message: "Success"})
        console.log('check4',department);
      }).catch(err=>{
        this.setState({alert_message: "Error"})
        console.log('WTF',err)
      })
  }
  render() {
    if(this.state.loggedIn === false){
      return <Redirect to="/user-profile-lite" />
    }
    const { service_name , service_des , service_price } = this.state
    return (
      <Container fluid className="main-content-container px-4 pb-4">
        <Row noGutters className="page-header py-4">
          <PageTitle sm="4" title="Add New Service" subtitle="Services" className="text-sm-left" />
        </Row>
        <Row>
         <Col lg="12">
            {this.state.alert_message==="Success"?
            <Alert theme="success">
              Service Added Successfully!
            </Alert> :null
            }
          </Col>
        </Row> 
        <Row>
          <Col lg="1">
          </Col>
          <Col lg="3">
          <ListGroupItem className="p-0 px-3 pt-3"> 
              <Col sm="12" className="mb-3">
                  <strong className="text-muted d-block mb-2">Select Department</strong>
                  { this.state.department ?  this.renderRadioButtons() :
                  <strong> fetching data... </strong>}
                  {/* <strong> Selected ID is: {this.selectID}</strong>
                  <strong> Selected Value is: {this.selectName}</strong> */}
              </Col>
          </ListGroupItem>
          </Col>
          <Col lg="6">
            <Card small className="mb-3">
              <CardBody>
                <Form className="add-new-post" onSubmit={this.submitHandler}>
                  <FormInput 
                    size="lg"
                    className="mb-3"
                    placeholder="Service Title"
                    name="service_name"
                    value={service_name}
                    onChange={this.changeHandler}
                  />
                 
                  <FormInput 
                    size="lg" 
                    className="mb-3" 
                    placeholder="Service Description" 
                    name="service_des"
                    value={service_des}
                    onChange={this.changeHandler}
                  />
                  <FormInput 
                    size="lg" 
                    className="mb-3" 
                    placeholder="Service Initial Price (PKR)" 
                    name="service_price"
                    value={service_price}
                    onChange={this.changeHandler}
                  />

                  <Button type = "submit" theme="accent">Create New Service</Button>
                </Form>
              </CardBody>
            </Card>
          </Col>
        </Row>
        {/* <Row>
          <Col lg="3">
          </Col>
          <Col lg="5">
            <Alert theme="success">
              Alert - {this.message}
            </Alert>
        </Col>
       </Row> */}
      </Container>
      
    )
  } 
}

export default AddNewDept;
