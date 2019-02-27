let attr = [];
let id;
let file;
let attachedFiles = [];
let counter = 0;

$(document).ready(function () {
    CKEDITOR.replace('editor1');
    let link = window.location.href;
    id = link.split('/');
    id = id[id.length - 1];
    getAllEmails(id);
});

function getAllEmails(id) {
    let url = '/mail/getAllEmails';
    let data = {id: id};
    $.ajax({
        type: "POST",
        url: url,
        data: data,

        success: function (mails) {
            let mailList = document.getElementById("mailList");

            for (let i = 0; i < mails.length; i++) {
                let mail = document.createElement('div');
                mail.setAttribute('style', 'cursor:pointer');
                mail.setAttribute('onclick', 'getMailContent(' + mails[i].sentDateMills + ')');

                let mailContentHolder = document.createElement('div');
                let mailHeaderHolder = document.createElement('div');
                mailContentHolder.setAttribute('id', 'mailContentHolder_' + mails[i].sentDateMills);
                mailContentHolder.style.display = 'none';
                mailHeaderHolder.setAttribute('id', 'mailHeaderHolder_' + mails[i].sentDateMills);
                mail.appendChild(mailHeaderHolder);
                mail.appendChild(mailContentHolder);

                let hr = document.createElement('hr');
                let mailSentDate = mails[i].sentDate;
                let mailSubject = mails[i].subject;
                let mailContent = mails[i].content;

                mailContentHolder.append(mailContent);
                mailHeaderHolder.innerHTML = mailSentDate + '---' + mailSubject;

                if (!mails[i].seen)
                    mailHeaderHolder.style.fontWeight = 'bold';

                mailContentHolder.innerHTML = mailContent;
                mailList.appendChild(hr);
                mailList.appendChild(mail);
            }
        }
    });
}

let xhr;
function getMailContent(mail_sentDateMills) {
    if (typeof xhr !== 'undefined'){
        xhr.abort();
        console.log('ajax call has been aborted');
    }

    $('#pre').remove();
    $('#attachment_holder').remove();

    document.getElementById('skEditorHolder').style.display = 'none';
    document.getElementById('sendButton').style.display = 'none';
    document.getElementById('replyButton').style.display = 'none';
    document.getElementById('dragNdropContainer').style.display = 'none';

    document.getElementById('mailHeaderHolder_' + mail_sentDateMills).style.fontWeight = 'normal';

    let url = '/mail/getEmailContentAndAttachment';
    let data = {sentDateMills: mail_sentDateMills};

    let pre = document.createElement('pre');
    pre.setAttribute('id', 'pre');
    pre.setAttribute('style', 'word-wrap: break-word');

let attachment_holder;
 xhr = $.ajax({
        method: 'POST',
        url:url,
        data:data,

        success: function (emailContentAndAttachment) {
            let mailContentContainer = document.getElementById('mailContentContainer');
            pre.append(emailContentAndAttachment.content);

            let replyButton = document.getElementById('replyButton');
            replyButton.setAttribute('onclick', 'replyToEmail()');
            replyButton.style.display = 'block';

            if (emailContentAndAttachment.attachments.length !== 0){
                attachment_holder = document.createElement('div');
                attachment_holder.setAttribute("id", "attachment_holder");
                emailContentAndAttachment.attachments.forEach(attachment => {

                        let attachment_container = document.createElement('div');
                        let a = document.createElement('a');
                        let file_name = attachment.split('\\');
                        file_name = file_name[file_name.length - 1];

                        a.setAttribute('id', file_name);
                        a.setAttribute('href', "/emails/downLoad/" + file_name);
                        a.setAttribute('src', "/emails/downLoad/" + file_name);
                        a.setAttribute('download', file_name);
                        a.appendChild(document.createTextNode(file_name));
                        attachment_container.appendChild(a);
                        attachment_holder.appendChild(attachment_container);
                });
                if (typeof attachment_holder !== 'undefined'){
                    mailContentContainer.append(attachment_holder);
                }
            }
        }
    });
    mailContentContainer.prepend(pre);
}

function replyToEmail() {
    CKEDITOR.instances.editor1.setData('');
    document.getElementById('skEditorHolder').style.display = 'block';
    document.getElementById('replyButton').style.display = 'none';
    document.getElementById('sendButton').style.display = 'block';
    document.getElementById('dragNdropContainer').style.display = 'block';

    let dragNdrop = $('#dragNdropContainer');
    let maxFileSize = 10000000;

    if (typeof (window.FileReader) === 'undefined') {
        dragNdrop.text('Не поддерживается');
        dragNdrop.addClass('error');
    }
    else {
        dragNdrop.text('Перетащите файлы для загрузки');
        dragNdrop[0].ondragover = function () {
            dragNdrop.addClass('hover');
            return false;
        };

        dragNdrop[0].ondragleave = function () {
            dragNdrop.removeClass('hover');
            return false;
        };

        dragNdrop[0].ondrop = function (event) {
            event.preventDefault();
            dragNdrop.removeClass('hover');
            dragNdrop.addClass('drop');

            file = event.dataTransfer.files[0];
            if (file.size > maxFileSize) {
                dragNdrop.text('Вложение слишком большое');
                dragNdrop.addClass('error');
                return false;
            }else {
                attachedFiles[counter] = file;
                let a = document.createElement('a');
                a.setAttribute('onclick', 'deleteAttachment(' + counter + ')');
                a.innerHTML = 'X ';

                let att = document.getElementById('attachments');
                let attachment = document.createElement('span');
                attachment.setAttribute('id', counter);
                attachment.innerHTML = file.name + " ";
                attachment.appendChild(a);
                att.appendChild(attachment);
                counter++;
            }
        }
    }
    let sendButton = document.getElementById('sendButton');
    sendButton.setAttribute('onclick', 'sendEmail()');
}

function sendEmail() {
    CKEDITOR.instances.editor1.config.allowedContent = false;
    let mailContent = CKEDITOR.instances.editor1.getData();

    if (mailContent !== '') {
        let formData = new FormData();
        formData.append('id', id);
        formData.append('mailContent', mailContent);

        attachedFiles.forEach(attachedFile => {
            formData.append('attachments', attachedFile);
        });

        let url = '/emails/sendMessage';
        $.ajax({
            url: url,
            enctype: 'multipart/form-data',
            method: 'POST',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            dataType: 'json',
            success: function () {
                document.getElementById('skEditorHolder').style.display = 'none';
                document.getElementById('sendButton').style.display = 'none';
                document.getElementById('replyButton').style.display = 'block';
                document.getElementById('dragNdropContainer').style.display = 'none';
                $('#dragNdropContainer').removeClass('drop');
                document.getElementById('attachments').innerHTML = '';
                attachedFiles = [];
                counter = 0;
            },
            error: function () {
                alert("Не удалось отправить сообщение");
            }
        });
    }
}
function deleteAttachment(i) {
    $('#'+i).remove();
    attachedFiles.splice(i,1);
}