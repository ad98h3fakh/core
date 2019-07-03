const t=window.dotcmsFields.h;import{a as e}from"./chunk-1d89c98b.js";import{b as s,j as a,a as l,c as i,d as r,f as n,g as u,h as o,m as h}from"./chunk-098a701f.js";const d=({label:t,value:e})=>({key:t,value:e});class m{constructor(){this.disabled=!1,this.hint="",this.label="",this.name="",this.required=!1,this.requiredMessage="This field is required",this.value="",this.items=[]}valueWatch(){this.value=s(this,"value","string"),this.items=a(this.value).map(d)}reset(){this.items=[],this.value="",this.status=l(this.isValid()),this.emitChanges()}deleteItemHandler(t){t.stopImmediatePropagation(),this.items=this.items.filter((e,s)=>s!==t.detail),this.refreshStatus(),this.emitChanges()}addItemHandler({detail:t}){this.items=[...this.items,t],this.refreshStatus(),this.emitChanges()}componentWillLoad(){this.validateProps(),this.setOriginalStatus(),this.emitStatusChange()}hostData(){return{class:i(this.status,this.isValid(),this.required)}}render(){return t(e,null,t("dot-label",{"aria-describedby":r(this.hint),tabIndex:this.hint?0:null,label:this.label,required:this.required,name:this.name},t("key-value-form",{onLostFocus:this.blurHandler.bind(this),"add-button-label":this.formAddButtonLabel,disabled:this.isDisabled(),"key-label":this.formKeyLabel,"key-placeholder":this.formKeyPlaceholder,"value-label":this.formValueLabel,"value-placeholder":this.formValuePlaceholder}),t("key-value-table",{onClick:t=>{t.preventDefault()},"button-label":this.listDeleteLabel,disabled:this.isDisabled(),items:this.items})),n(this.hint),u(this.showErrorMessage(),this.getErrorMessage()))}isDisabled(){return this.disabled||null}blurHandler(){this.status.dotTouched||(this.status=o(this.status,{dotTouched:!0}),this.emitStatusChange())}validateProps(){this.valueWatch()}setOriginalStatus(){this.status=l(this.isValid())}isValid(){return!(this.required&&!this.items.length)}showErrorMessage(){return this.getErrorMessage()&&!this.status.dotPristine}getErrorMessage(){return this.isValid()?"":this.requiredMessage}refreshStatus(){this.status=o(this.status,{dotTouched:!0,dotPristine:!1,dotValid:this.isValid()})}emitStatusChange(){this.statusChange.emit({name:this.name,status:this.status})}emitValueChange(){const t=h(this.items);this.valueChange.emit({name:this.name,value:t})}emitChanges(){this.emitStatusChange(),this.emitValueChange()}static get is(){return"dot-key-value"}static get properties(){return{disabled:{type:Boolean,attr:"disabled",reflectToAttr:!0},el:{elementRef:!0},formAddButtonLabel:{type:String,attr:"form-add-button-label",reflectToAttr:!0},formKeyLabel:{type:String,attr:"form-key-label",reflectToAttr:!0},formKeyPlaceholder:{type:String,attr:"form-key-placeholder",reflectToAttr:!0},formValueLabel:{type:String,attr:"form-value-label",reflectToAttr:!0},formValuePlaceholder:{type:String,attr:"form-value-placeholder",reflectToAttr:!0},hint:{type:String,attr:"hint",reflectToAttr:!0},items:{state:!0},label:{type:String,attr:"label",reflectToAttr:!0},listDeleteLabel:{type:String,attr:"list-delete-label",reflectToAttr:!0},name:{type:String,attr:"name",reflectToAttr:!0},required:{type:Boolean,attr:"required",reflectToAttr:!0},requiredMessage:{type:String,attr:"required-message",reflectToAttr:!0},reset:{method:!0},status:{state:!0},value:{type:String,attr:"value",reflectToAttr:!0,mutable:!0,watchCallbacks:["valueWatch"]}}}static get events(){return[{name:"valueChange",method:"valueChange",bubbles:!0,cancelable:!0,composed:!0},{name:"statusChange",method:"statusChange",bubbles:!0,cancelable:!0,composed:!0}]}static get listeners(){return[{name:"delete",method:"deleteItemHandler"},{name:"add",method:"addItemHandler"}]}static get style(){return""}}const b={key:"",value:""};class c{constructor(){this.disabled=!1,this.addButtonLabel="Add",this.keyPlaceholder="",this.valuePlaceholder="",this.keyLabel="Key",this.valueLabel="Value",this.inputs=Object.assign({},b)}render(){const e=this.isButtonDisabled();return t("form",{onSubmit:this.addKey.bind(this)},t("label",null,this.keyLabel,t("input",{disabled:this.disabled,name:"key",onBlur:t=>this.lostFocus.emit(t),onInput:t=>this.setValue(t),placeholder:this.keyPlaceholder,type:"text",value:this.inputs.key})),t("label",null,this.valueLabel,t("input",{disabled:this.disabled,name:"value",onBlur:t=>this.lostFocus.emit(t),onInput:t=>this.setValue(t),placeholder:this.valuePlaceholder,type:"text",value:this.inputs.value})),t("button",{class:"key-value-form__save__button",type:"submit",disabled:e},this.addButtonLabel))}isButtonDisabled(){return!this.isFormValid()||this.disabled||null}isFormValid(){return!(!this.inputs.key.length||!this.inputs.value.length)}setValue(t){t.stopImmediatePropagation();const e=t.target;this.inputs=Object.assign({},this.inputs,{[e.name]:e.value.toString()})}addKey(t){t.preventDefault(),t.stopImmediatePropagation(),this.inputs.key&&this.inputs.value&&(this.add.emit(this.inputs),this.clearForm(),this.focusKeyInputField())}clearForm(){this.inputs=Object.assign({},b)}focusKeyInputField(){this.el.querySelector('input[name="key"]').focus()}static get is(){return"key-value-form"}static get properties(){return{addButtonLabel:{type:String,attr:"add-button-label",reflectToAttr:!0},disabled:{type:Boolean,attr:"disabled",reflectToAttr:!0},el:{elementRef:!0},inputs:{state:!0},keyLabel:{type:String,attr:"key-label",reflectToAttr:!0},keyPlaceholder:{type:String,attr:"key-placeholder",reflectToAttr:!0},valueLabel:{type:String,attr:"value-label",reflectToAttr:!0},valuePlaceholder:{type:String,attr:"value-placeholder",reflectToAttr:!0}}}static get events(){return[{name:"add",method:"add",bubbles:!0,cancelable:!0,composed:!0},{name:"lostFocus",method:"lostFocus",bubbles:!0,cancelable:!0,composed:!0}]}static get style(){return"key-value-form form{display:-ms-flexbox;display:flex;-ms-flex-align:center;align-items:center}key-value-form form button{margin:0}key-value-form form input{margin:0 1rem 0 .5rem}key-value-form form label{display:-ms-flexbox;display:flex;-ms-flex-align:center;align-items:center}key-value-form form label,key-value-form form label input{-ms-flex-positive:1;flex-grow:1}"}}class p{constructor(){this.items=[],this.disabled=!1,this.buttonLabel="Delete",this.emptyMessage="No values"}render(){return t("table",null,t("tbody",null,this.renderRows(this.items)))}onDelete(t){this.delete.emit(t)}getRow(e,s){return t("tr",null,t("td",null,t("button",{"aria-label":`${this.buttonLabel} ${e.key}, ${e.value}`,disabled:this.disabled||null,onClick:()=>this.onDelete(s),class:"dot-key-value__delete-button"},this.buttonLabel)),t("td",null,e.key),t("td",null,e.value))}renderRows(t){return this.isValidItems(t)?t.map(this.getRow.bind(this)):this.getEmptyRow()}getEmptyRow(){return t("tr",null,t("td",null,this.emptyMessage))}isValidItems(t){return Array.isArray(t)&&!!t.length}static get is(){return"key-value-table"}static get properties(){return{buttonLabel:{type:String,attr:"button-label",reflectToAttr:!0},disabled:{type:Boolean,attr:"disabled",reflectToAttr:!0},emptyMessage:{type:String,attr:"empty-message",reflectToAttr:!0},items:{type:"Any",attr:"items"}}}static get events(){return[{name:"delete",method:"delete",bubbles:!0,cancelable:!0,composed:!0}]}}export{m as DotKeyValue,c as KeyValueForm,p as KeyValueTable};