let j = require('jsforce');


let sfdcConn = new j.Connection({loginUrl: 'https://test.salesforce.com'});
sfdcConn.login(
    'francisco.meza@invitae.com.stglis', 'notthereal1mLqeugENV3rXi28XUbuH2pZXX');
    // (err, success) => {
    //     console.log('err', err);
    //     console.log('success', success);
    // });
