import React from "react";
import { Container, Row, Col, Card, CardHeader, Button, ButtonGroup, CardBody } from "shards-react";

import axios from 'axios';
import PageTitle from "../components/common/PageTitle";
//import {storage} from '../firebase';
//import { Document, Page } from 'react-pdf';

class Service extends React.Component {
  state = {
   vendor: null,
   selectedVendor:'',
   picturePath: '',
   finalUrl: ''
  //  numPages: null,
  //  pageNumber: 1
 } 

 componentDidMount(){
   axios.get(`http://localhost:3005/api/fetch_pending_vendors`)
     .then(res => {
       const vendor = res.data;
       this.setState({ vendor });
       console.log('check1',vendor);
     }).catch(err=>{
       console.log('always add catch after then',err)
     })
 }
 changeVendor = (selectedVendor) =>{
   this.setState({selectedVendor})
 }
//  onDocumentLoadSuccess = ({ numPages }) => {
//   this.setState({ numPages });
// }
 renderUserData = () => {
  //const { pageNumber } = this.state;
   return this.state.vendor.map(elem=>{
     return <tr
       onChange={() => {
         this.changeVendor(elem.user_id);
         this.changeVendor(elem.user_name);
         this.changeVendor(elem.user_email);
         this.changeVendor(elem.user_phone);
         this.changeVendor(elem.user_address);
         this.changeVendor(elem.user_cnic);
         this.changeVendor(elem.user_image);
       }}
     >
      <td>
       {elem.user_id}
      </td>
      <td>
       {elem.user_name}
       </td>
      <td>
       {elem.user_address}
       </td>
      <td>
       {elem.user_phone}
       </td>
       <td>
       {elem.user_cnic}
       </td>
      <td>
        <a href={elem.user_image.replace(/^"(.*)"$/, '$1')}>
          <img src={elem.user_image.replace(/^"(.*)"$/, '$1')} width="80" height="50" alt=""/>       
        </a>
       </td>
       <td>
       {elem.user_portfolio}
       </td>
       <td>
        <div className="blog-comments__actions">
          <ButtonGroup size="sm">
            <Button theme="white" onClick={()=>this.updatePendingStatus(elem.user_id)}>
              <span className="text-success">
                <i className="material-icons">check</i>
              </span>{" "}
              Approve
            </Button>
            <Button theme="white" onClick={()=>this.updateRejectStatus(elem.user_id)}>
              <span className="text-danger">
                <i className="material-icons">clear</i>
              </span>{" "}
              Reject
            </Button>
          </ButtonGroup>
        </div>
      </td>
     </tr>
   })
 }
 updatePendingStatus(userId)
  {
    axios.put(`http://localhost:3005/api/update_pen_verification_status/${userId}`)
      .then(res => {
        const { vendor }  = this.state;
        this.setState({ 
          vendor: vendor.filter(vendor1 => vendor1.user_id !== userId)
         });
        console.log('check1',vendor);
      }).catch(err=>{
        console.log('always add catch after then',err)
      })
  }
  updateRejectStatus(userId)
  {
    axios.put(`http://localhost:3005/api/update_rej_verification_status/${userId}`)
      .then(res => {
        const { vendor }  = this.state;
        this.setState({ 
          vendor: vendor.filter(vendor1 => vendor1.user_id !== userId)
         });
        console.log('check1',vendor);
      }).catch(err=>{
        console.log('always add catch after then',err)
      })
  }
 render() {
   
   return (
      <Container fluid className="main-content-container px-4">
      <Row noGutters className="page-header py-4">
        <PageTitle sm="4" title="All Pending Workers" subtitle="Workers" className="text-sm-left" />
      </Row>
      <Row>
        <Col>
          <Card small className="mb-4">
            <CardHeader className="border-bottom">
              <h6 className="m-0">Pending Workers</h6>
            </CardHeader>
            <CardBody className="p-0 pb-3">
              <table className="table mb-0">
                <thead className="bg-light">
                  <tr>
                    <th scope="col" className="border-0">#</th>
                    <th scope="col" className="border-0">Name</th>
                    <th scope="col" className="border-0">Address</th>
                    <th scope="col" className="border-0">Phone</th>
                    <th scope="col" className="border-0">CNIC</th>
                    <th scope="col" className="border-0">CNIC Img</th>
                    <th scope="col" className="border-0">Porfolio</th>
                    <th scope="col" className="border-0">Actions</th>
                  </tr>
                </thead>
                <tbody>
                { this.state.vendor ?  this.renderUserData() :
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
export default Service;