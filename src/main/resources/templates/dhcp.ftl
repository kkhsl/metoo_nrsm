<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DHCP</title>
    <style>



        table
        {
            border-collapse:collapse;
        }
        table,
        th,
        td {
            border: 1px solid black;
            align:"center";
        }
        p {
            text-align:center
        }
    </style>
</head>
<body>

<#-- list 数据的展示 -->

<p>展示DHCP数据:</p>

<br>
<br>
<table  border="1" align="center" cellpadding="10" cellspacing="0">
    <tr>
        <th>lease</th>
        <th>starts</th>
        <th>ends</th>
        <th>tstp</th>
        <th>cltt</th>
        <th>binding_state</th>
        <th>next_binding_state</th>
        <th>rewind_binding_state</th>
        <th>hardware_ethernet</th>
        <th>uid</th>
        <th>client_hostname</th>
    </tr>

    <#list dhcps as dhcp>
         <tr>
             <td>${dhcp.lease?if_exists}</td>
             <td>${dhcp.starts?if_exists}</td>
             <td>${dhcp.ends?if_exists}</td>
             <td>${dhcp.tstp?if_exists}</td>
             <td>${dhcp.cltt?if_exists}</td>
             <td>${dhcp.binding_state?if_exists}</td>
             <td>${dhcp.next_binding_state?if_exists}</td>
             <td>${dhcp.rewind_binding_state?if_exists}</td>
             <td>${dhcp.hardware_ethernet?if_exists}</td>
             <td>${dhcp.uid?if_exists}</td>
             <td>${dhcp.client_hostname?if_exists}</td>
         </tr>
    </#list>
</table>


</body>
</html>
