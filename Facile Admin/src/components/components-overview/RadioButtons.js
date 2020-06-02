import React from "react";
import { Col, FormRadio } from "shards-react";

import axios from 'axios';

class RadioButtons extends React.Component {
   state = {
    department: null,
    selectedDepartment:'',
    selectName: '',
    selectID: ''
  } 

  componentDidMount(){
    axios.get(`https://facile-app.herokuapp.com/api/get_department`)
      .then(res => {
        const department = res.data;
        this.setState({ department });
        console.log('check1',department);
      }).catch(err=>{
        console.log('always add catch after then',err)
      })
  }
  changeDepartment = (selectedDepartment) =>{
    this.setState({selectedDepartment})
  }
  renderRadioButtons = () => {
    return this.state.department.map(elem=>{
      return <FormRadio
        checked={this.state.selectedDepartment === elem.department_name}
        onChange={() => {
          this.changeDepartment(elem.department_id);
          this.changeDepartment(elem.department_name);
          this.selectID = elem.department_id;
          this.selectName = elem.department_name;
        }}
      >
        {elem.department_name}
      </FormRadio>
    })
  }
  render() {
    return (
      <Col sm="12" className="mb-3">
      <strong className="text-muted d-block mb-2">Select Departments</strong>
      { this.state.department ?  this.renderRadioButtons() :
      <strong> fetching data... </strong>}
      {/* <strong> Selected ID is: {this.selectID}</strong>
      <strong> Selected Value is: {this.selectName}</strong> */}
      </Col>
    )
  } 
}

export default RadioButtons;
