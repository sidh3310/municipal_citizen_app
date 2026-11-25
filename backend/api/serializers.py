from rest_framework import serializers
from rest_framework_simplejwt.tokens import RefreshToken
from .models import User, Complaint, Department, Notification, Assignment, Announcement

class MyTokenObtainPairSerializer(serializers.Serializer):
    email = serializers.CharField(required=False)
    username = serializers.CharField(required=False)
    password = serializers.CharField()

    def validate(self, attrs):
        email = attrs.get('email') or attrs.get('username')
        password = attrs.get('password')

        if not email or not password:
            raise serializers.ValidationError({'detail': 'Email and password are required.'})

        email = email.strip()

        try:
            user = User.objects.get(email__iexact=email)
        except User.DoesNotExist:
            raise serializers.ValidationError({'detail': 'No account found with this email address.'})

        if not user.check_password(password):
            raise serializers.ValidationError({'detail': 'Incorrect password. Please try again.'})

        if not user.is_active:
            raise serializers.ValidationError({'detail': 'This user account is inactive.'})

        refresh = RefreshToken.for_user(user)
        refresh['role'] = user.role
        refresh['email'] = user.email

        return {
            'refresh': str(refresh),
            'access': str(refresh.access_token),
            'role': user.role,
            'email': user.email,
            'full_name': user.full_name
        }

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'email', 'full_name', 'phone', 'role', 'password')
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        user = User.objects.create_user(**validated_data)
        return user

class ComplaintSerializer(serializers.ModelSerializer):
    user_name = serializers.ReadOnlyField(source='user.full_name')

    class Meta:
        model = Complaint
        fields = '__all__'
        read_only_fields = ('user',)

class DepartmentSerializer(serializers.ModelSerializer):
    class Meta:
        model = Department
        fields = '__all__'

class NotificationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Notification
        fields = '__all__'

class AssignmentSerializer(serializers.ModelSerializer):
    class Meta:
        model = Assignment
        fields = '__all__'

class AnnouncementSerializer(serializers.ModelSerializer):
    class Meta:
        model = Announcement
        fields = '__all__'
