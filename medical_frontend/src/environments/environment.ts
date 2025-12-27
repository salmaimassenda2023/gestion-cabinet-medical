export const environment = {
  production: false,
  apiUrl: 'http://localhost:8222', // Gateway URL
  keycloak: {
    url: 'http://localhost:9098',
    realm: 'cabinet-medical',
    clientId: 'cabinet-medical-frontend'
  },
  stripe: {
    publishableKey: 'pk_test_51Qve47GMvHZk4oqlujb2fLiPlBGCMI4jc7cSZn1V96f0sQOGRr5sytMocls5Nk8cZkjVjcKwDL6i79ZM384C3mDM00D73zVWps'
  }
};
