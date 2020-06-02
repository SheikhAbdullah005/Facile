import React from "react";
import { Card, CardBody, Form, Button, FormInput } from "shards-react";
import CustomFileUpload from "../components-overview/CustomFileUpload";

const Editor = () => (
  <Card small className="mb-3">
    <CardBody>
      <Form className="add-new-post">
        <FormInput size="lg" className="mb-3" placeholder="Service Title" />
        <FormInput size="lg" className="mb-3" placeholder="Service Description" />
  
        <strong className="text-muted d-block mb-3">
          Custom File Upload
        </strong>
        <CustomFileUpload />

        <FormInput size="lg" className="mb-3" placeholder="Service Intial Price" />
        <Button type = "submit" theme="accent">Create New Service</Button>
      </Form>
    </CardBody>
  </Card>
);

export default Editor;
