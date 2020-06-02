import React from "react";
import {
   Container, Card, CardHeader, ListGroup, ListGroupItem, Row, Col, Form, FormInput, Button } from "shards-react";
import PageTitle from "../components/common/PageTitle";
import { Redirect } from 'react-router-dom';

class UserProfileLite extends React.Component {

  constructor(props) {
    super(props)
    const token = localStorage.getItem("token")

    let loggedIn = true
    if(token == null){
      loggedIn = false
    }
    
    this.state = {
      username: '',
      password: '',
      loggedIn
    }
    this.onChange = this.onChange.bind(this)
    this.submitForm = this.submitForm.bind(this)
  }

  onChange(e){
    this.setState({
      [e.target.name]: e.target.value
    })
  }

  submitForm(e){
    e.preventDefault()
    const { username, password } = this.state
    //Login Code 
    if(username === "admin@facile.com" && password === "admin"){
      localStorage.setItem("token", "fdnjnfjdfnjdnfscmdkm")
      this.setState({
        loggedIn: true
      })
    }
  }

  render() {
    if(this.state.loggedIn){
      return <Redirect to="/add-new-post" />
    }
    return (
    <Container fluid className="main-content-container px-4">
      <Row noGutters className="page-header py-4">
        <PageTitle title="User Login" subtitle="Overview" md="12" className="ml-sm-auto mr-sm-auto" />
      </Row>
      <Row>
        <Col lg="10">
          <Card small className="mb-4">
            <CardHeader className="border-bottom">
              <h6 className="m-0">Enter Your Login Details</h6>
            </CardHeader>
            <ListGroup flush>
              <ListGroupItem className="p-3">
                <Row>
                  <Col>
                    <Form onSubmit={this.submitForm}>
                      <Row form>
                        <Col md="12" className="form-group">
                          <label htmlFor="feEmail">Email</label>
                          <FormInput
                            type="email"
                            id="feEmail"
                            placeholder="Email Address"
                            name = "username"
                            value={this.state.username}
                            onChange={this.onChange}
                            autoComplete="username"
                          />
                        </Col>
                        <Col md="12" className="form-group">
                          <label htmlFor="fePassword">Password</label>
                          <FormInput
                            type="password"
                            id="fePassword"
                            placeholder="Password"
                            name = "password"
                            value={this.state.password}
                            onChange={this.onChange}
                            autoComplete="current-password"
                          />
                        </Col>
                      </Row>
                      <Button type="submit" theme="accent">Login</Button>
                    </Form>
                  </Col>
                </Row>
              </ListGroupItem>
            </ListGroup>
          </Card>
          </Col>
        <Col lg="2">
        </Col>
      </Row>
    </Container>
    )
  }
}

export default UserProfileLite;
