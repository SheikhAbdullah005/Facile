import React from "react";
import { Container, Row, Col,Button, ButtonGroup, Card, CardHeader, CardBody } from "shards-react";

import axios from 'axios';
import PageTitle from "../components/common/PageTitle";

class Customer extends React.Component {
  state = {
   customer: null,
   selectedCustomer:'',
   picturePath: '',
   finalUrl: ''
 } 

 componentDidMount(){
   axios.get(`http://localhost:3005/api/all_customers`)
     .then(res => {
       const customer = res.data;
       this.setState({ customer });
       console.log('check1',customer);
     }).catch(err=>{
       console.log('always add catch after then',err)
     })
 }
 changeCustomer = (selectedCustomer) =>{
   this.setState({selectedCustomer})
 }
 renderCustomerData = () => {
   return this.state.customer.map(elem=>{
     return <tr
       onChange={() => {
         this.changeCustomer(elem.user_id);
         this.changeCustomer(elem.user_name);
         this.changeCustomer(elem.user_email);
         this.changeCustomer(elem.user_phone);
         this.changeCustomer(elem.user_cnic);
         this.changeCustomer(elem.user_image);
       }}
     >
      <td>
       {elem.user_id}
      </td>
      <td>
       {elem.user_name}
       </td>
       <td>
       {elem.user_email}
       </td>
      <td>
       {elem.user_phone}
       </td>
       <td>
       {elem.user_cnic}
       </td>
      <td>
       <img src={elem.user_image.replace(/^"(.*)"$/, '$1')} width="80" height="50" alt=""/>
       </td>
       <td>
        <div className="blog-comments__actions">
          <ButtonGroup size="sm">
            <Button theme="white" onClick={()=>this.deleteUser(elem.user_id)}>
              <span className="text-danger">
                <i className="material-icons">clear</i>
              </span>{" "}
              Delete
            </Button>
          </ButtonGroup>
        </div>
      </td>
     </tr>
   })
 }
 deleteUser(userId)
 {
   axios.put(`http://localhost:3005/api/delete_user/${userId}`)
     .then(res => {
       console.log('Id: ', userId);
       console.log('state: ', this.state);
       const { customer }  = this.state;
       this.setState({ 
        customer: customer.filter(customer1 => customer1.user_id !== userId)
        });
       console.log('check10',customer);
     }).catch(err=>{
       console.log('WTF',err)
     })
 }
 render() {
   return (
      <Container fluid className="main-content-container px-4">
      {/* Page Header */}
      <Row noGutters className="page-header py-4">
        <PageTitle sm="4" title="All Customers" subtitle="Customers" className="text-sm-left" />
      </Row>

      {/* Default Light Table */}
      <Row>
        <Col>
          <Card small className="mb-4">
            <CardHeader className="border-bottom">
              <h6 className="m-0">Customers</h6>
            </CardHeader>
            <CardBody className="p-0 pb-3">
              <table className="table mb-0">
                <thead className="bg-light">
                  <tr>
                    <th scope="col" className="border-0">#</th>
                    <th scope="col" className="border-0">Name</th>
                    <th scope="col" className="border-0">Email</th>
                    <th scope="col" className="border-0">Phone</th>
                    <th scope="col" className="border-0">CNIC</th>
                    <th scope="col" className="border-0">CNIC Img</th>
                    <th scope="col" className="border-0">Action</th>
                  </tr>
                </thead>
                <tbody>
                { this.state.customer ?  this.renderCustomerData() :
                <strong> fetching data... </strong>}
            
                </tbody>
              </table>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </Container>
   )
 } 
}

export default Customer;
