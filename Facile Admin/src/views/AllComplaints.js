import React from "react";
import { Container, Row, Col, Card, CardHeader, CardBody } from "shards-react";
import axios from 'axios';
import PageTitle from "../components/common/PageTitle";

class Complaint extends React.Component {
  state = {
   complaint: null,
   selectedComplaint:''
 } 

 componentDidMount(){
   axios.get(`http://localhost:3005/api/all_complaints`)
     .then(res => {
       const complaint = res.data;
       this.setState({ complaint });
       console.log('check1',complaint);
     }).catch(err=>{
       console.log('always add catch after then',err)
     })
 }
 changeComplaint = (selectedComplaint) =>{
   this.setState({selectedComplaint})
 }
 renderComplaintsData = () => {
   return this.state.complaint.map(elem=>{
     return <tr
       onChange={() => {
         this.changeComplaint(elem.complaint_id);
         this.changeComplaint(elem.complaint_message);
         this.changeComplaint(elem.against_user_name);
       }}
     >
      <td>
       {elem.complaint_id}
      </td>
      <td>
       {elem.complaint_message}
       </td>
       <td>
       {elem.against_user_name}
       </td>
     </tr>
   })
 }
 render() {
   return (
      <Container fluid className="main-content-container px-4">
      {/* Page Header */}
      <Row noGutters className="page-header py-4">
        <PageTitle sm="4" title="All Complaints" subtitle="Users" className="text-sm-left" />
      </Row>
      <Row>
        <Col>
          <Card small className="mb-4">
            <CardHeader className="border-bottom">
              <h6 className="m-0">All Compaint Users</h6>
            </CardHeader>
            <CardBody className="p-0 pb-3">
              <table className="table mb-0">
                <thead className="bg-light">
                  <tr>
                    <th scope="col" className="border-0">#</th>
                    <th scope="col" className="border-0">Complaints Message</th>
                    <th scope="col" className="border-0">Complainted User Name</th>
                  </tr>
                </thead>
                <tbody>
                { this.state.complaint ?  this.renderComplaintsData() :
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

export default Complaint;
