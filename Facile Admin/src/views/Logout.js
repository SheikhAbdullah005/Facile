import React from "react";
import { Container, Row, Col, Card, CardHeader } from "shards-react";
import PageTitle from "../components/common/PageTitle";
import { Link } from 'react-router-dom'

class Logout extends React.Component {

  constructor(props) {
    super(props)
    localStorage.removeItem("token")
  }

  render() {
    return (
      <Container fluid className="main-content-container px-4">
        <Row noGutters className="page-header py-4">
          <PageTitle title="You've Been Logged Out From The System" subtitle="Logout" md="12" className="ml-sm-auto mr-sm-auto" />
        </Row>
        <Row>
        <Col lg="2">
        </Col>
        <Col lg="4">
          <Card small className="mb-4">
            <CardHeader className="border-bottom">
              <h6 className="m-0">Click Link Below</h6>
              <Link to="/user-profile-lite"> Go to Login Page Again </Link>
            </CardHeader>
          </Card>
         </Col> 

        </Row>
      </Container>
    )  
  }
}

export default Logout;